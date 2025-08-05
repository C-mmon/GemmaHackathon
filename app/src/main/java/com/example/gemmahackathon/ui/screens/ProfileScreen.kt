package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gemmahackathon.ui.components.*
import com.example.gemmahackathon.viewModel.DiaryViewModel
import com.example.gemmahackathon.viewModel.UserViewModel

@Composable
fun ProfileScreen(
    diaryViewModel: DiaryViewModel,
    userViewModel: UserViewModel
) {
    val user by userViewModel.user.collectAsState()
    val uiState by diaryViewModel.uiState.collectAsState()
    val mood by diaryViewModel.mood.collectAsState()
    val tone by diaryViewModel.tone.collectAsState()
    val writingStyle by diaryViewModel.writingStyle.collectAsState()
    
    // Load latest entry data for profile display
    LaunchedEffect(uiState.entries) {
        val latestEntry = uiState.entries.firstOrNull()
        latestEntry?.let {
            diaryViewModel.loadMood(it.diaryEntry.id)
            diaryViewModel.loadTone(it.diaryEntry.id)
            diaryViewModel.loadWritingStyle(it.diaryEntry.id)
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
                text = "Profile",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A2BE2), // Purple color for text
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            // User Info Card
            DiaryCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape),
                        color = user?.visualMoodColour?.let { 
                            try { Color(android.graphics.Color.parseColor(it)) } 
                            catch (e: Exception) { MaterialTheme.colorScheme.primary }
                        } ?: MaterialTheme.colorScheme.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Profile",
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Name
                    Text(
                        text = user?.name ?: "Dear Diary User",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8A2BE2) // Purple color for text
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // About
                    Text(
                        text = user?.about ?: "Welcome to your diary journey",
                        fontSize = 16.sp,
                        color = Color(0xFF8A2BE2).copy(alpha = 0.7f) // Purple color for text
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Edit button (placeholder)
                    OutlinedButton(
                        onClick = { /* TODO: Implement edit profile */ }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Profile")
                    }
                }
            }
            
            // Current Mood Card
            DiaryCard {
                SectionHeader("Current Mood Analysis")
                Spacer(modifier = Modifier.height(8.dp))
                
                if (mood != null || tone != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (mood != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mood:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8A2BE2) // Purple color for text
                                )
                                MoodChip(mood = mood)
                            }
                        }
                        
                        if (tone != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tone:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8A2BE2) // Purple color for text
                                )
                                Text(
                                    text = tone!!,
                                    fontSize = 14.sp,
                                    color = Color(0xFF8A2BE2).copy(alpha = 0.8f) // Purple color for text
                                )
                            }
                        }
                        
                        if (writingStyle != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Writing Style:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF8A2BE2) // Purple color for text
                                )
                                Text(
                                    text = writingStyle!!,
                                    fontSize = 14.sp,
                                    color = Color(0xFF8A2BE2).copy(alpha = 0.8f) // Purple color for text
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Write an entry to see your mood analysis",
                        fontSize = 14.sp,
                        color = Color(0xFF8A2BE2).copy(alpha = 0.6f) // Purple color for text
                    )
                }
            }
            
            // User Characteristics Card
            DiaryCard {
                SectionHeader("Your Characteristics")
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    user?.thinkingStyle?.let { style ->
                        CharacteristicRow("Thinking Style", style)
                    }
                    
                    user?.learningStyle?.let { style ->
                        CharacteristicRow("Learning Style", style)
                    }
                    
                    user?.writingStyle?.let { style ->
                        CharacteristicRow("Writing Style", style)
                    }
                    
                    user?.emotionalStrength?.let { strength ->
                        CharacteristicRow("Emotional Strength", strength)
                    }
                    
                    user?.emotionalWeakness?.let { weakness ->
                        CharacteristicRow("Emotional Challenge", weakness)
                    }
                    
                    user?.moodSensitivityLevel?.let { level ->
                        CharacteristicRow("Mood Sensitivity", "$level/10")
                    }
                    
                    if (user?.thinkingStyle == null && user?.learningStyle == null && user?.writingStyle == null) {
                        Text(
                            text = "Your characteristics will appear here as you write more entries",
                            fontSize = 14.sp,
                            color = Color(0xFF8A2BE2).copy(alpha = 0.6f) // Purple color for text
                        )
                    }
                }
            }
            
            // Statistics Card
            DiaryCard {
                SectionHeader("Your Statistics")
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatisticColumn(
                        value = "${uiState.entries.size}",
                        label = "Entries",
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    StatisticColumn(
                        value = "${uiState.entries.flatMap { it.tags }.distinctBy { it.name }.size}",
                        label = "Unique Tags",
                        color = MaterialTheme.colorScheme.secondary
                    )
                    
                    StatisticColumn(
                        value = "${calculateStreak(uiState.entries)}",
                        label = "Day Streak",
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            // Emotional Signature Card (if available)
            user?.emotionalSignature?.let { signature ->
                DiaryCard {
                    SectionHeader("Your Emotional Signature")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = signature,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8A2BE2), // Purple color for text
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacteristicRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF8A2BE2), // Purple color for text
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF8A2BE2).copy(alpha = 0.8f), // Purple color for text
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatisticColumn(
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

private fun calculateStreak(entries: List<com.example.gemmahackathon.data.diary.DiaryWithTags>): Int {
    if (entries.isEmpty()) return 0
    
    val sortedEntries = entries.sortedByDescending { it.diaryEntry.dateMillis }
    val calendar = java.util.Calendar.getInstance()
    val today = java.util.Calendar.getInstance()
    
    var streak = 0
    var currentDate = today.timeInMillis
    
    for (entry in sortedEntries) {
        val entryCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = entry.diaryEntry.dateMillis
        }
        
        calendar.timeInMillis = currentDate
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(java.util.Calendar.YEAR)
        
        val entryDay = entryCalendar.get(java.util.Calendar.DAY_OF_YEAR)
        val entryYear = entryCalendar.get(java.util.Calendar.YEAR)
        
        if (currentDay == entryDay && currentYear == entryYear) {
            streak++
            calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
            currentDate = calendar.timeInMillis
        } else {
            break
        }
    }
    
    return streak
}