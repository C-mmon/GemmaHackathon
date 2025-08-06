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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemmahackathon.ui.components.*
import com.example.gemmahackathon.viewModel.DiaryViewModel


data class MoodData(
    val date: Long,
    val mood: String?,
    val confidence: Float?,
    val stressLevel: Int?
)


@Composable
fun MoodTrackerScreen(diaryViewModel: DiaryViewModel) {
    /******* Kick Of load **/
    val uiState          by diaryViewModel.uiState.collectAsState()

    val moodMap          by diaryViewModel.moodMap.collectAsState()
    val confidenceMap    by diaryViewModel.confidenceMap.collectAsState()
    val stressMap        by diaryViewModel.stressMap.collectAsState()
    val emotionStrMap    by diaryViewModel.emotionMap.collectAsState()

    LaunchedEffect(uiState.entries) {
        uiState.entries.forEach { entry ->
            val id = entry.diaryEntry.id
            diaryViewModel.loadMood(id)
            diaryViewModel.loadMoodConfidence(id)
            diaryViewModel.loadStressLevel(id)
            diaryViewModel.loadEmotionDistribution(id)
        }
    }

    /**Build a chart**/
    val allMoodData = remember(moodMap, confidenceMap, stressMap) {
        uiState.entries.mapNotNull { entry ->
            val id   = entry.diaryEntry.id
            val mood = moodMap[id]
            val conf = confidenceMap[id]
            val str  = stressMap[id]
            // Include entry if at least mood is available
            if (mood != null) {
                MoodData(entry.diaryEntry.dateMillis, mood, conf, str)
            } else null
        }.sortedBy { it.date }
    }

    val emotionTotals = remember(emotionStrMap) {
        val totals = mutableMapOf<String, Float>()
        uiState.entries.forEach { entry ->
            val raw = emotionStrMap[entry.diaryEntry.id] ?: return@forEach
            try {
                // Try to parse as JSON first
                val json = org.json.JSONObject(raw)
                json.keys().forEach { key ->
                    val value = json.optDouble(key, 0.0).toFloat()
                    if (value > 0f) totals[key] = (totals[key] ?: 0f) + value
                }
            } catch (e: Exception) {
                // Fallback to comma-separated format
                raw.split(',').forEach { token ->
                    val parts = token.trim().split(':')
                    if (parts.size == 2) {
                        val key = parts[0].trim()
                        val v   = parts[1].trim().toFloatOrNull() ?: 0f
                        if (v > 0f) totals[key] = (totals[key] ?: 0f) + v
                    }
                }
            }
        }
        totals.toMap()
    }
    /* ------------- UI ------------------------- */
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
                val latest = allMoodData.last()
                DiaryCard {
                    SectionHeader("Current Mood")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            MoodChip(mood = latest.mood)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Latest Mood",
                                fontSize = 12.sp,
                                color = Color(0xFF8A2BE2) // Purple color for text
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${((latest.confidence ?: 0f) * 100).toInt()}%",
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
                                text = "${latest.stressLevel ?: 0}/10",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = when ((latest.stressLevel ?: 0)) {
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
                if (emotionTotals.isNotEmpty()) {
                    DiaryCard {
                        SectionHeader("Emotion Distribution")
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        EmotionPieChart(
                            emotions = emotionTotals,
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
                            text = "• ${emotionTotals.size} different emotions detected",
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