package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemmahackathon.ui.components.*
import com.example.gemmahackathon.viewModel.DiaryViewModel
import kotlin.random.Random

data class TagWithCount(
    val name: String,
    val count: Int,
    val color: Color,
    val fontSize: Int
)

@Composable
fun TagCloudScreen(
    diaryViewModel: DiaryViewModel
) {
    val uiState by diaryViewModel.uiState.collectAsState()
    
    val tagCounts = remember(uiState.entries) {
        val allTags = uiState.entries.flatMap { it.tags.map { tag -> tag.name } }
        val tagCountMap = allTags.groupingBy { it }.eachCount()
        
        tagCountMap.map { (tag, count) ->
            val maxCount = tagCountMap.values.maxOrNull() ?: 1
            val normalizedCount = count.toFloat() / maxCount
            
            TagWithCount(
                name = tag,
                count = count,
                color = generateTagColor(tag),
                fontSize = (12 + (normalizedCount * 20)).toInt().coerceAtLeast(12).coerceAtMost(32)
            )
        }.sortedByDescending { it.count }
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
                text = "Tag Cloud",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (tagCounts.isEmpty()) {
                // Empty state
                DiaryCard {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text = "🏷️",
                            fontSize = 48.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tags yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Write some diary entries and tags will appear here",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                // Statistics Card
                DiaryCard {
                    SectionHeader("Tag Statistics")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tagCounts.size}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Unique Tags",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${tagCounts.sumOf { it.count }}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Total Tags",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tagCounts.firstOrNull()?.name ?: "—",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                text = "Most Used",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                // Tag Cloud
                DiaryCard {
                    SectionHeader("Your Tag Cloud")
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TagCloudLayout(tags = tagCounts)
                }
                
                // Top Tags List
                DiaryCard {
                    SectionHeader("Most Frequent Tags")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    tagCounts.take(10).forEachIndexed { index, tagWithCount ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${index + 1}.",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.width(24.dp)
                                )
                                TagChip(tag = tagWithCount.name)
                            }
                            
                            Text(
                                text = "${tagWithCount.count}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TagCloudLayout(tags: List<TagWithCount>) {
    // Simple flow layout for tag cloud
    var currentRowWidth by remember { mutableStateOf(0) }
    val maxWidth = 280 // Approximate max width in dp
    
    Column {
        var currentRow = mutableListOf<TagWithCount>()
        var rows = mutableListOf<List<TagWithCount>>()
        
        tags.forEach { tag ->
            val tagWidth = tag.name.length * (tag.fontSize * 0.6).toInt() + 32 // Approximate width
            
            if (currentRowWidth + tagWidth > maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow.toList())
                currentRow = mutableListOf(tag)
                currentRowWidth = tagWidth
            } else {
                currentRow.add(tag)
                currentRowWidth += tagWidth + 8 // Add spacing
            }
        }
        
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }
        
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                row.forEach { tagWithCount ->
                    Text(
                        text = tagWithCount.name,
                        fontSize = tagWithCount.fontSize.sp,
                        color = tagWithCount.color,
                        fontWeight = if (tagWithCount.count > 2) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private fun generateTagColor(tag: String): Color {
    val colors = listOf(
        Color(0xFF2196F3), // Blue
        Color(0xFF4CAF50), // Green  
        Color(0xFFFF9800), // Orange
        Color(0xFF9C27B0), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF00BCD4), // Cyan
        Color(0xFF8BC34A), // Light Green
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF3F51B5), // Indigo
        Color(0xFF795548)  // Brown
    )
    
    val hash = tag.hashCode()
    return colors[kotlin.math.abs(hash) % colors.size]
}