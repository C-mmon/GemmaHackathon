package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
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
            
            // Personality Profile Card - Modern Glass Morphism
            ModernProfileCard {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = "Your Personality Profile",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Visual Mood Color Display
                    user?.visualMoodColour?.let { colorHex ->
                        ModernAttributeCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🎨 Mood Color",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    // Mood Color Blob
                                    val moodColor = try {
                                        Color(android.graphics.Color.parseColor(colorHex))
                                    } catch (e: Exception) {
                                        MaterialTheme.colorScheme.primary
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        moodColor.copy(alpha = 0.9f),
                                                        moodColor.copy(alpha = 0.6f),
                                                        moodColor.copy(alpha = 0.3f)
                                                    )
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                }
                                Text(
                                    text = colorHex,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    // Mood Sensitivity Level
                    user?.moodSensitivityLevel?.let { level ->
                        ModernAttributeCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📊 Mood Sensitivity",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$level/10",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (level) {
                                        in 1..3 -> Color(0xFF10B981) // Green
                                        in 4..7 -> Color(0xFFF59E0B) // Amber
                                        else -> Color(0xFFEF4444) // Red
                                    }
                                )
                            }
                        }
                    }
                    
                    // Thinking Style
                    user?.thinkingStyle?.let { style ->
                        ModernAttributeCard {
                            ModernCharacteristicRow("🧠 Thinking Style", style)
                        }
                    }
                    
                    // Learning Style
                    user?.learningStyle?.let { style ->
                        ModernAttributeCard {
                            ModernCharacteristicRow("📚 Learning Style", style)
                        }
                    }
                    
                    // Writing Style
                    user?.writingStyle?.let { style ->
                        ModernAttributeCard {
                            ModernCharacteristicRow("✍️ Writing Style", style)
                        }
                    }
                    
                    // Emotional Strength
                    user?.emotionalStrength?.let { strength ->
                        ModernAttributeCard {
                            ModernCharacteristicRow("💪 Emotional Strength", strength)
                        }
                    }
                    
                    // Emotional Weakness
                    user?.emotionalWeakness?.let { weakness ->
                        ModernAttributeCard {
                            ModernCharacteristicRow("🔍 Areas to Grow", weakness)
                        }
                    }
                    
                    // Emotional Signature
                    user?.emotionalSignature?.let { signature ->
                        ModernAttributeCard {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "🎭 Emotional Signature",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = signature,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                    
                    // Show beautiful empty state if no characteristics are available
                    if (user?.thinkingStyle == null && user?.learningStyle == null && 
                        user?.writingStyle == null && user?.emotionalStrength == null && 
                        user?.emotionalWeakness == null && user?.emotionalSignature == null &&
                        user?.visualMoodColour == null && user?.moodSensitivityLevel == null) {
                        ModernEmptyState()
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

// Modern Glass Morphism Components for Profile
@Composable
private fun ModernProfileCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        content()
    }
}

@Composable
private fun ModernAttributeCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun ModernCharacteristicRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun ModernEmptyState(
    modifier: Modifier = Modifier
) {
    ModernAttributeCard(modifier = modifier) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "🌱",
                fontSize = 48.sp
            )
            Text(
                text = "Your Personality Profile is Growing",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Keep writing diary entries to discover insights about your thinking patterns, emotional strengths, and personal style. Our AI will gradually build your unique personality profile.",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
        }
    }
}