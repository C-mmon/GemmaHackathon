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
import kotlin.math.max
import kotlin.math.min

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

    
    // Load mood data for recent entries
    LaunchedEffect(uiState.entries) {
        uiState.entries.forEach { entry ->
            diaryViewModel.loadMood(entry.diaryEntry.id)
            diaryViewModel.loadMoodConfidence(entry.diaryEntry.id)
            diaryViewModel.loadStressLevel(entry.diaryEntry.id)
        }
    }
    
    val mood by diaryViewModel.mood.collectAsState()
    val moodConfidence by diaryViewModel.moodConfidence.collectAsState()
    val stressLevel by diaryViewModel.stressLevel.collectAsState()
    
    // Generate mock data for visualization (in a real app, you'd collect this over time)
    val moodData = remember(uiState.entries) {
        generateMoodData(uiState.entries, mood, moodConfidence, stressLevel)
    }
    
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
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (moodData.isEmpty()) {
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
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Write diary entries to track your mood over time",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Mood Over Time Chart
                DiaryCard {
                    SectionHeader("Mood Over Time")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    MoodChart(
                        data = moodData,
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
                        data = moodData,
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
                        data = moodData,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
                
                // Insights
                DiaryCard {
                    SectionHeader("Insights")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val avgStress = moodData.mapNotNull { it.stressLevel }.average()
                    val avgConfidence = moodData.mapNotNull { it.confidence }.average()
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "• Average stress level: ${String.format("%.1f", avgStress)}/10",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• Average confidence: ${String.format("%.0f", avgConfidence * 100)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• You've tracked ${moodData.size} mood${if (moodData.size != 1) "s" else ""} so far",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
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

private fun generateMoodData(
    entries: List<com.example.gemmahackathon.data.diary.DiaryWithTags>,
    currentMood: String?,
    currentConfidence: Float?,
    currentStress: Int?
): List<MoodData> {
    // In a real app, you'd store historical mood data
    // For demo purposes, generate some sample data based on entries
    return entries.take(7).mapIndexed { index, entry ->
        MoodData(
            date = entry.diaryEntry.dateMillis,
            mood = if (index == 0) currentMood else listOf("happy", "sad", "neutral", "anxious", "calm").random(),
            confidence = if (index == 0) currentConfidence else (0.3f + Math.random() * 0.7f).toFloat(),
            stressLevel = if (index == 0) currentStress else (1..8).random()
        )
    }.reversed()
}