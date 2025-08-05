package com.example.gemmahackathon.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.gemmahackathon.ui.components.*
import com.example.gemmahackathon.viewModel.DiaryViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    diaryViewModel: DiaryViewModel
) {
    val uiState by diaryViewModel.uiState.collectAsState()
    val reflectionQuestions by diaryViewModel.reflectionQuestions.collectAsState()
    val summary by diaryViewModel.summary.collectAsState()
    val events by diaryViewModel.events.collectAsState(initial = null)

    //Diary is backed by remember {mutableState of "", this is making diary a state, so when we update it diaryText, the UI recomposes
    var diaryText by remember { mutableStateOf("") }

    var lastCreatedEntryId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    
    // Handle one-shot events
    LaunchedEffect(events) {
        events?.let { event ->
            when (event) {
                is com.example.gemmahackathon.viewModel.DiaryUiEvent.ShowError -> {
                    // Show snackbar or toast for error
                }
            }
        }
    }
    
    // Load summary when a new entry is created
    LaunchedEffect(uiState.entries.size) {
        val latestEntry = uiState.entries.firstOrNull()
        if (latestEntry != null && latestEntry.diaryEntry.id != lastCreatedEntryId) {
            lastCreatedEntryId = latestEntry.diaryEntry.id
            diaryViewModel.loadSummary(latestEntry.diaryEntry.id)
            diaryViewModel.loadReflectionQuestions(latestEntry.diaryEntry.id)
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Title
            Text(
                text = "Dear Diary",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Reflection Questions Section
            DiaryCard {
                SectionHeader("Today's Reflection")
                Spacer(modifier = Modifier.height(8.dp))
                val reflectionColor = Color(0xFF64B5F6) // This is the color of the reflection question
                if (!reflectionQuestions.isNullOrBlank()) {
                    Column {
                        reflectionQuestions!!.split(",").forEach { question ->
                            Text(
                                text = "• ${question.trim()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = reflectionColor,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

            }

            // Diary Entry Input Section
            DiaryCard {
                SectionHeader("Write Your Entry")
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = diaryText,
                    onValueChange = { diaryText = it },
                    label = { Text("Share your thoughts...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    minLines = 4,
                    maxLines = 6,
                    enabled = !uiState.isLoading
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        if (diaryText.isNotBlank()) {
                            scope.launch {

                                //There is some race condition, cannot figure out why
                                val textShow = diaryText
                                diaryText=""
                                diaryViewModel.createEntry(textShow)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = diaryText.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Creating...")
                    } else {
                        Icon(Icons.Default.Send, contentDescription = "Send")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Entry")
                    }
                }
            }

            // Recent Entry Summary
            if (summary != null && !uiState.isLoading) {
                DiaryCard {
                    SectionHeader("Entry Summary")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = summary!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF64B5F6),
                        lineHeight = 18.sp
                    )
                }
            }

            // Recent Entries Preview
            if (uiState.entries.isNotEmpty()) {
                DiaryCard {
                    SectionHeader("Recent Entries")
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    uiState.entries.take(3).forEach { entryWithTags ->
                        Column(
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = entryWithTags.diaryEntry.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64B5F6),
                                maxLines = 2
                            )
                            
                            if (entryWithTags.tags.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    entryWithTags.tags.take(3).forEach { tag ->
                                        TagChip(tag = tag.name)
                                    }
                                    if (entryWithTags.tags.size > 3) {
                                        Text(
                                            text = "+${entryWithTags.tags.size - 3}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64B5F6)
                                        )
                                    }
                                }
                            }
                            
                            if (entryWithTags != uiState.entries.take(3).last()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(
                                    color = Color(0xFF64B5F6)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}