package com.example.gemmahackathon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemmahackathon.ui.theme.GradientStart
import com.example.gemmahackathon.ui.theme.GradientEnd

/**
 * Common gradient background used across all screens
 */
@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            )
    ) {
        content()
    }
}

/**
 * Loading indicator with message
 */
@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

/**
 * Card with consistent styling and gradient overlay
 */
@Composable
fun DiaryCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFF0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Section header with consistent styling
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,

        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFFFF8DA1),
        textAlign = TextAlign.Start
    )
}

/**
 * Mood indicator chip
 */
@Composable
fun MoodChip(
    mood: String?,
    modifier: Modifier = Modifier
) {
    if (mood != null) {
        Surface(
            modifier = modifier.clip(RoundedCornerShape(16.dp)),
            color = when (mood.lowercase()) {
                "happy", "joy", "excited" -> Color(0xFF4CAF50)
                "sad", "depressed", "down" -> Color(0xFFF44336)
                "anxious", "worried", "stressed" -> Color(0xFFFF9800)
                else -> MaterialTheme.colorScheme.primary
            }
        ) {
            Text(
                text = mood,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Tag chip for displaying diary entry tags
 */
@Composable
fun TagChip(
    tag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    ) {
        Text(
            text = tag,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color(0xFF64B5F6),
            fontSize = 12.sp
        )
    }
}

/**
 * Error message display
 */
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = Color(0xFFD32F2F),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}