package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemmahackathon.ui.components.*
import com.example.gemmahackathon.viewModel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

data class MoodData(
    val date: Long,
    val mood: String?,
    val confidence: Float?,
    val stressLevel: Int?
)

@Composable
fun MoodTrackerScreen(
    diaryViewModel: DiaryViewModel
) {
    val uiState by diaryViewModel.uiState.collectAsState()
    val emotionDistribution by diaryViewModel.emotionDistribution.collectAsState()

    // State to hold real mood data from all entries
    var allMoodData by remember { mutableStateOf<List<MoodData>>(emptyList()) }
    var emotionDistributions by remember { mutableStateOf<Map<String, Float>>(emptyMap()) }
    
    // Load real mood data for all entries
    LaunchedEffect(uiState.entries) {
        val moodDataList = mutableListOf<MoodData>()
        val emotionMap = mutableMapOf<String, Float>()
        
        uiState.entries.forEach { entry ->
            diaryViewModel.loadMood(entry.diaryEntry.id)
            diaryViewModel.loadMoodConfidence(entry.diaryEntry.id)
            diaryViewModel.loadStressLevel(entry.diaryEntry.id)
            diaryViewModel.loadEmotionDistribution(entry.diaryEntry.id)
            
            // Collect real data for each entry
            val entryMood = diaryViewModel.mood.value
            val entryConfidence = diaryViewModel.moodConfidence.value
            val entryStress = diaryViewModel.stressLevel.value
            val entryEmotions = diaryViewModel.emotionDistribution.value

            Log.d("MoodTrackerScreen", "Mood: $entryMood, Confidence: $entryConfidence, Stress: $entryStress, Emotions: $entryEmotions")
            
            moodDataList.add(
                MoodData(
                    date = entry.diaryEntry.dateMillis,
                    mood = entryMood,
                    confidence = entryConfidence,
                    stressLevel = entryStress
                )
            )
            
            // Parse emotion distribution (assuming it's a JSON string or similar)
            entryEmotions?.let { emotions ->
                // Simple parsing - adjust based on actual format
                emotions.split(",").forEach { emotion ->
                    val parts = emotion.trim().split(":")
                    if (parts.size == 2) {
                        val emotionName = parts[0].trim()
                        val emotionValue = parts[1].trim().toFloatOrNull() ?: 0f
                        if (emotionValue > 0) {
                            emotionMap[emotionName] = emotionMap.getOrDefault(emotionName, 0f) + emotionValue
                        }
                    }
                }
            }
        }
        
        allMoodData = moodDataList.sortedBy { it.date }
        emotionDistributions = emotionMap
    }
    
    val mood by diaryViewModel.mood.collectAsState()
    val moodConfidence by diaryViewModel.moodConfidence.collectAsState()
    val stressLevel by diaryViewModel.stressLevel.collectAsState()
    
    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Text(
                text = "Mood Tracker",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF06292), // Pink color for text
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (allMoodData.isEmpty()) {
                // Empty state
                DiaryCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "📊",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No mood data yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Write diary entries to track your mood over time",
                            fontSize = 14.sp,
                            color = Color(0xFF8A2BE2).copy(alpha = 0.7f) // Purple color for text
                        )
                    }
                }
            } else {
                // Current Mood Summary
                DiaryCard {
                    SectionHeader("Current Mood")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MoodChip(mood = mood)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Latest Mood",
                                fontSize = 12.sp,
                                color = Color(0xFF8A2BE2) // Purple color for text
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${((moodConfidence ?: 0f) * 100).toInt()}%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Confidence",
                                fontSize = 12.sp,
                                color = Color(0xFF8A2BE2) // Purple color for text
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${stressLevel ?: 0}/10",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = when ((stressLevel ?: 0)) {
                                    in 0..3 -> Color.Green
                                    in 4..6 -> Color.Yellow
                                    else -> Color.Red
                                }
                            )
                            Text(
                                text = "Stress Level",
                                fontSize = 12.sp,
                                color = Color(0xFF8A2BE2) // Purple color for text
                            )
                        }
                    }
                }
                
                // Emotion Distribution Pie Chart
                if (emotionDistributions.isNotEmpty()) {
                    DiaryCard {
                        SectionHeader("Emotion Distribution")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        EmotionPieChart(
                            emotions = emotionDistributions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }
                }
                
                // Mood Over Time Chart
                DiaryCard {
                    SectionHeader("Mood Over Time")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MoodChart(
                        data = allMoodData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                
                // Confidence Over Time Chart
                DiaryCard {
                    SectionHeader("Mood Confidence")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ConfidenceChart(
                        data = allMoodData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                
                // Stress Level Over Time Chart
                DiaryCard {
                    SectionHeader("Stress Level")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StressChart(
                        data = allMoodData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                
                // Insights
                DiaryCard {
                    SectionHeader("Insights")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val avgStress = allMoodData.mapNotNull { it.stressLevel }.average()
                    val avgConfidence = allMoodData.mapNotNull { it.confidence }.average()
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "• Average stress level: ${String.format("%.1f", if (avgStress.isNaN()) 0.0 else avgStress)}/10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                        Text(
                            text = "• Average confidence: ${String.format("%.0f", if (avgConfidence.isNaN()) 0.0 else avgConfidence * 100)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                        Text(
                            text = "• You've tracked ${allMoodData.size} mood${if (allMoodData.size != 1) "s" else ""} so far",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                        Text(
                            text = "• ${emotionDistributions.size} different emotions detected",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmotionPieChart(
    emotions: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF00BCD4), // Cyan
        Color(0xFFFFEB3B), // Yellow
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )
    
    Canvas(modifier = modifier) {
        if (emotions.isEmpty()) return@Canvas
        
        val total = emotions.values.sum()
        if (total <= 0) return@Canvas
        
        val center = Offset(size.width / 2, size.height / 2)
        val radius = kotlin.math.min(size.width, size.height) / 2.5f
        
        var startAngle = 0f
        val sortedEmotions = emotions.toList().sortedByDescending { it.second }
        
        // Draw pie slices
        sortedEmotions.forEachIndexed { index, (emotion, value) ->
            val sweepAngle = (value / total) * 360f
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )
            
            startAngle += sweepAngle
        }
        
        // Draw legend
        var legendY = 20f
        sortedEmotions.forEachIndexed { index, (emotion, value) ->
            val color = colors[index % colors.size]
            val percentage = (value / total * 100).toInt()
            
            // Draw color indicator
            drawCircle(
                color = color,
                radius = 8.dp.toPx(),
                center = Offset(20.dp.toPx(), legendY)
            )
            
            // Draw text would require more complex implementation
            // For now, we'll just show the pie chart
            legendY += 25.dp.toPx()
        }
    }
}

@Composable
private fun MoodChart(
    data: List<MoodData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        // Draw axes
        drawLine(
            color = Color.Gray,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw mood points
        val moodColors = mapOf(
            "happy" to Color.Green,
            "sad" to Color.Red,
            "anxious" to Color.Yellow,
            "calm" to Color.Blue,
            "excited" to Color.Magenta,
            "neutral" to Color.Gray
        )
        
        data.forEachIndexed { index, moodData ->
            val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
            val y = height - padding - 50.dp.toPx() // Fixed height for mood points
            
            val color = moodColors[moodData.mood?.lowercase()] ?: Color.Gray
            drawCircle(
                color = color,
                radius = 8.dp.toPx(),
                center = Offset(x, y)
            )
        }
    }
}

@Composable
private fun ConfidenceChart(
    data: List<MoodData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        // Draw axes
        drawLine(
            color = Color.Gray,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw confidence line
        val path = Path()
        data.forEachIndexed { index, moodData ->
            val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
            val confidence = moodData.confidence ?: 0f
            val y = height - padding - (confidence * (height - 2 * padding))
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // Draw point
            drawCircle(
                color = Color.Blue,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
        }
        
        drawPath(
            path = path,
            color = Color.Blue,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
    }
}

@Composable
private fun StressChart(
    data: List<MoodData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        
        // Draw axes
        drawLine(
            color = Color.Gray,
            start = Offset(padding, height - padding),
            end = Offset(width - padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        drawLine(
            color = Color.Gray,
            start = Offset(padding, padding),
            end = Offset(padding, height - padding),
            strokeWidth = 2.dp.toPx()
        )
        
        // Draw stress bars
        data.forEachIndexed { index, moodData ->
            val x = padding + (index.toFloat() / (data.size - 1)) * (width - 2 * padding)
            val stress = (moodData.stressLevel ?: 0) / 10f
            val barHeight = stress * (height - 2 * padding)
            val y = height - padding - barHeight
            
            val color = when (moodData.stressLevel ?: 0) {
                in 0..3 -> Color.Green
                in 4..6 -> Color.Yellow
                else -> Color.Red
            }
            
            drawRect(
                color = color,
                topLeft = Offset(x - 10.dp.toPx(), y),
                size = androidx.compose.ui.geometry.Size(20.dp.toPx(), barHeight)
            )
        }
    }
}

// Removed generateMoodData function - now using real data from backend