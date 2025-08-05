package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemmahackathon.ui.components.*
import com.example.gemmahackathon.viewModel.DiaryViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

@Composable
fun CalendarScreen(
    diaryViewModel: DiaryViewModel
) {
    val uiState by diaryViewModel.uiState.collectAsState()
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var selectedEntry by remember { mutableStateOf<com.example.gemmahackathon.data.diary.DiaryWithTags?>(null) }
    
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance().time
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    // Get days in current month
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfMonth = calendar.time
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val startDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
    
    val daysWithEntries = remember(uiState.entries) {
        uiState.entries.groupBy { entry ->
            val entryCalendar = Calendar.getInstance().apply {
                timeInMillis = entry.diaryEntry.dateMillis
            }
            "${entryCalendar.get(Calendar.YEAR)}-${entryCalendar.get(Calendar.MONTH)}-${entryCalendar.get(Calendar.DAY_OF_MONTH)}"
        }
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
                text = "Calendar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF06292).copy(alpha = 0.7f), // Pink color for text
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // Calendar Card
            DiaryCard {
                // Month/Year header
                Text(
                    text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(firstDayOfMonth),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A2BE2).copy(alpha = 0.7f), // Purple color for text, // Purple color for text
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Day of week headers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                        Text(
                            text = day,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF8A2BE2).copy(alpha = 0.7f), // Purple color for text
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Calendar grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Empty cells for days before the first day of the month
                    items(startDayOfWeek) {
                        Spacer(modifier = Modifier.size(32.dp))
                    }
                    
                    // Days of the month
                    items(daysInMonth) { dayIndex ->
                        val day = dayIndex + 1
                        val dayKey = "$currentYear-$currentMonth-$day"
                        val hasEntry = daysWithEntries.containsKey(dayKey)
                        val dayCalendar = Calendar.getInstance().apply {
                            set(currentYear, currentMonth, day)
                        }
                        val isToday = abs(dayCalendar.timeInMillis - today.time) < 24 * 60 * 60 * 1000
                        
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isToday -> MaterialTheme.colorScheme.primary
                                        hasEntry -> MaterialTheme.colorScheme.secondary
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable {
                                    selectedDate = dayCalendar.time
                                    selectedEntry = daysWithEntries[dayKey]?.firstOrNull()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.toString(),
                                fontSize = 14.sp,
                                color = when {
                                    isToday -> Color.White
                                    hasEntry -> Color.White
                                    else -> Color(0xFF8A2BE2) // Purple color for text
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legend
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Today",
                            fontSize = 12.sp,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "Has Entry",
                            fontSize = 12.sp,
                            color = Color(0xFF8A2BE2) // Purple color for text
                        )
                    }
                }
            }
            
            // Streak Information
            DiaryCard {
                SectionHeader("Your Streak")
                Spacer(modifier = Modifier.height(8.dp))
                
                val streak = calculateStreak(uiState.entries)
                Text(
                    text = "🔥 $streak day${if (streak != 1) "s" else ""} streak!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF8A2BE2).copy(alpha = 0.7f) // Purple color for text
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Keep it up! Consistency is key to meaningful reflection.",
                    fontSize = 14.sp,
                    color = Color(0xFF8A2BE2).copy(alpha = 0.7f) // Purple color for text
                )
            }
            
            // Selected Entry Details
            selectedEntry?.let { entry ->
                DiaryCard {
                    SectionHeader("Entry for ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(selectedDate)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = entry.diaryEntry.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8A2BE2) ,// Purple color for text,
                        lineHeight = 20.sp                    )

                    if (entry.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            entry.tags.forEach { tag ->
                                TagChip(tag = tag.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun calculateStreak(entries: List<com.example.gemmahackathon.data.diary.DiaryWithTags>): Int {
    if (entries.isEmpty()) return 0
    
    val sortedEntries = entries.sortedByDescending { it.diaryEntry.dateMillis }
    val calendar = Calendar.getInstance()
    val today = Calendar.getInstance()
    
    var streak = 0
    var currentDate = today.timeInMillis
    
    for (entry in sortedEntries) {
        val entryCalendar = Calendar.getInstance().apply {
            timeInMillis = entry.diaryEntry.dateMillis
        }
        
        // Check if entry is from current date
        calendar.timeInMillis = currentDate
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        
        val entryDay = entryCalendar.get(Calendar.DAY_OF_YEAR)
        val entryYear = entryCalendar.get(Calendar.YEAR)
        
        if (currentDay == entryDay && currentYear == entryYear) {
            streak++
            // Move to previous day
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            currentDate = calendar.timeInMillis
        } else {
            break
        }
    }
    
    return streak
}