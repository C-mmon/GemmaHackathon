package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
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
                // Current Mood Summary - Modern Glass Card
                val latest = allMoodData.last()
                ModernCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Current Mood",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ModernStatCard(
                                title = "Mood",
                                value = latest.mood?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                                color = getMoodColor(latest.mood)
                            )
                            
                            ModernStatCard(
                                title = "Confidence",
                                value = "${((latest.confidence ?: 0f) * 100).toInt()}%",
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            ModernStatCard(
                                title = "Stress",
                                value = "${latest.stressLevel ?: 0}/10",
                                color = getStressColor(latest.stressLevel ?: 0)
                            )
                        }
                    }
                }
                
                // Emotion Distribution - Modern
                if (emotionTotals.isNotEmpty()) {
                    ModernCard {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Emotion Distribution",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            ModernEmotionChart(
                                emotions = emotionTotals,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            )
                        }
                    }
                }
                
                // Mood Trends - Transparent Area Chart
                ModernCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Mood Trends",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        TransparentAreaChart(
                            data = allMoodData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                        )
                    }
                }
                
                // Insights - Modern
                ModernCard {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Insights",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        val avgStress = allMoodData.mapNotNull { it.stressLevel }.average()
                        val avgConfidence = allMoodData.mapNotNull { it.confidence }.average()
                        
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            InsightItem(
                                icon = "📊",
                                text = "Average stress level: ${String.format("%.1f", if (avgStress.isNaN()) 0.0 else avgStress)}/10"
                            )
                            InsightItem(
                                icon = "🎯",
                                text = "Average confidence: ${String.format("%.0f", if (avgConfidence.isNaN()) 0.0 else avgConfidence * 100)}%"
                            )
                            InsightItem(
                                icon = "📈",
                                text = "You've tracked ${allMoodData.size} mood${if (allMoodData.size != 1) "s" else ""} so far"
                            )
                            InsightItem(
                                icon = "🎭",
                                text = "${emotionTotals.size} different emotions detected"
                            )
                        }
                    }
                }
            }
        }
    }
}

// Modern Glass Morphism Card
@Composable
private fun ModernCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        content()
    }
}

@Composable
private fun ModernStatCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun InsightItem(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

// Modern Emotion Chart with proper text visibility
@Composable
private fun ModernEmotionChart(
    emotions: Map<String, Float>,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF8B5CF6), // Violet  
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4), // Cyan
        Color(0xFF10B981), // Emerald
        Color(0xFFF59E0B), // Amber
        Color(0xFFEF4444), // Red
        Color(0xFF84CC16), // Lime
    )
    
    Row(modifier = modifier) {
        // Chart
        Canvas(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            if (emotions.isEmpty()) return@Canvas
            
            val total = emotions.values.sum()
            if (total <= 0) return@Canvas
            
            val center = Offset(size.width / 2, size.height / 2)
            val radius = kotlin.math.min(size.width, size.height) / 3f
            
            var startAngle = -90f
            val sortedEmotions = emotions.toList().sortedByDescending { it.second }
            
            // Draw pie slices with modern styling
            sortedEmotions.forEachIndexed { index, (_, value) ->
                val sweepAngle = (value / total) * 360f
                val color = colors[index % colors.size]
                
                drawArc(
                    color = color.copy(alpha = 0.8f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
                )
                
                startAngle += sweepAngle
            }
        }
        
        // Legend with proper text colors
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            val total = emotions.values.sum()
            val sortedEmotions = emotions.toList().sortedByDescending { it.second }
            
            sortedEmotions.forEachIndexed { index, (emotion, value) ->
                val color = colors[index % colors.size]
                val percentage = (value / total * 100).toInt()
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color, RoundedCornerShape(6.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = emotion.replaceFirstChar { it.uppercase() },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$percentage%",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// Modern Transparent Area Chart
@Composable
private fun TransparentAreaChart(
    data: List<MoodData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val width = size.width
        val height = size.height
        val padding = 40.dp.toPx()
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding
        
        // Modern grid lines
        drawModernGrid(width, height, padding)
        
        // Confidence Area (background)
        if (data.any { it.confidence != null }) {
            val confidencePath = Path()
            val confidenceAreaPath = Path()
            
            data.forEachIndexed { index, moodData ->
                val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * chartWidth
                val confidence = moodData.confidence ?: 0f
                val y = height - padding - (confidence * chartHeight)
                
                if (index == 0) {
                    confidencePath.moveTo(x, y)
                    confidenceAreaPath.moveTo(x, height - padding)
                    confidenceAreaPath.lineTo(x, y)
                } else {
                    confidencePath.lineTo(x, y)
                    confidenceAreaPath.lineTo(x, y)
                }
                
                if (index == data.size - 1) {
                    confidenceAreaPath.lineTo(x, height - padding)
                    confidenceAreaPath.close()
                }
            }
            
            // Draw confidence area with gradient
            drawPath(
                path = confidenceAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = 0.3f),
                        Color(0xFF6366F1).copy(alpha = 0.1f)
                    )
                )
            )
            
            // Draw confidence line
            drawPath(
                path = confidencePath,
                color = Color(0xFF6366F1),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Stress Level Area (overlay)
        if (data.any { it.stressLevel != null }) {
            val stressPath = Path()
            val stressAreaPath = Path()
            
            data.forEachIndexed { index, moodData ->
                val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * chartWidth
                val stress = (moodData.stressLevel ?: 0) / 10f
                val y = height - padding - (stress * chartHeight)
                
                if (index == 0) {
                    stressPath.moveTo(x, y)
                    stressAreaPath.moveTo(x, height - padding)
                    stressAreaPath.lineTo(x, y)
                } else {
                    stressPath.lineTo(x, y)
                    stressAreaPath.lineTo(x, y)
                }
                
                if (index == data.size - 1) {
                    stressAreaPath.lineTo(x, height - padding)
                    stressAreaPath.close()
                }
            }
            
            // Draw stress area with gradient
            drawPath(
                path = stressAreaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFEF4444).copy(alpha = 0.2f),
                        Color(0xFFEF4444).copy(alpha = 0.05f)
                    )
                )
            )
            
            // Draw stress line
            drawPath(
                path = stressPath,
                color = Color(0xFFEF4444),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        
        // Modern data points (smaller and more elegant)
        data.forEachIndexed { index, moodData ->
            val x = padding + (index.toFloat() / (data.size - 1).coerceAtLeast(1)) * chartWidth
            
            // Confidence points
            moodData.confidence?.let { confidence ->
                val y = height - padding - (confidence * chartHeight)
                drawCircle(
                    color = Color(0xFF6366F1),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
            
            // Stress points  
            moodData.stressLevel?.let { stress ->
                val y = height - padding - ((stress / 10f) * chartHeight)
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        // Legend
        drawLegend()
    }
}

private fun DrawScope.drawModernGrid(width: Float, height: Float, padding: Float) {
    val gridColor = Color.Gray.copy(alpha = 0.2f)
    val strokeWidth = 1.dp.toPx()
    
    // Horizontal grid lines
    for (i in 1..4) {
        val y = padding + (i * (height - 2 * padding) / 5)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = strokeWidth
        )
    }
    
    // Vertical grid lines  
    for (i in 1..4) {
        val x = padding + (i * (width - 2 * padding) / 5)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawLegend() {
    val legendY = 20.dp.toPx()
    val legendX = 20.dp.toPx()
    
    // Confidence legend
    drawCircle(
        color = Color(0xFF6366F1),
        radius = 6.dp.toPx(),
        center = Offset(legendX, legendY)
    )
    
    // Stress legend
    drawCircle(
        color = Color(0xFFEF4444),
        radius = 6.dp.toPx(),
        center = Offset(legendX, legendY + 30.dp.toPx())
    )
}

// Helper functions for colors
private fun getMoodColor(mood: String?): Color {
    return when (mood?.lowercase()) {
        "positive", "happy", "joy" -> Color(0xFF10B981)
        "negative", "sad", "angry" -> Color(0xFFEF4444)
        "neutral" -> Color(0xFF6B7280)
        else -> Color(0xFF8B5CF6)
    }
}

private fun getStressColor(stressLevel: Int): Color {
    return when (stressLevel) {
        in 0..3 -> Color(0xFF10B981)  // Green
        in 4..6 -> Color(0xFFF59E0B)  // Amber
        else -> Color(0xFFEF4444)     // Red
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