package com.example.gemmahackathon.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemmahackathon.data.diary.DiaryDao
import com.example.gemmahackathon.data.diary.Tag
import com.example.gemmahackathon.domain.Logic.GemmaParser
import com.example.gemmahackathon.data.diary.DiaryEntry
import com.example.gemmahackathon.data.diary.DiaryWithTags
import com.example.gemmahackathon.domain.Logic.GemmaClient
import com.example.gemmahackathon.viewModel.UserViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log
import com.example.gemmahackathon.data.DiaryDatabase
import androidx.room.withTransaction
import com.example.gemmahackathon.data.user.UserEntity

/* ---------- Dispatcher abstraction for testability ---------- */
interface DispatcherProvider {
    val io: CoroutineDispatcher
    val main: CoroutineDispatcher
}

object DefaultDispatchers : DispatcherProvider {
    override val io: CoroutineDispatcher = Dispatchers.IO
    override val main: CoroutineDispatcher = Dispatchers.Main
}

data class DiaryUiState(
    val entries: List<DiaryWithTags> = emptyList(),
    val isLoading: Boolean = false,
    //Less bothered by the error
    )

sealed interface DiaryUiEvent {
    data class ShowError(val message: String) : DiaryUiEvent
}
/*******View Model******/
class DiaryViewModel(
    private val diaryDao: DiaryDao,
    private val gemmaClient: GemmaClient,
    private val userViewModel: UserViewModel,
    private val database: DiaryDatabase,
    private val dispatchers: DispatcherProvider = DefaultDispatchers,
) : ViewModel() {

    //state, THIS IS GLOBAL UI, does not need to change
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    //Converting all the single vaalue state flow into a map based flow
    private val _mood            = MutableStateFlow<String?>(null)
    val mood: StateFlow<String?> = _mood.asStateFlow()

    private val _moodConfidence            = MutableStateFlow<Float?>(null)
    val moodConfidence: StateFlow<Float?>  = _moodConfidence.asStateFlow()

    private val _stressLevel            = MutableStateFlow<Int?>(null)
    val stressLevel: StateFlow<Int?>   = _stressLevel.asStateFlow()

    private val _emotionDistribution            = MutableStateFlow<String?>(null)
    val emotionDistribution: StateFlow<String?> = _emotionDistribution.asStateFlow()

    /* --- per-entry maps used by the chart screen --- */
    private val _moodMap           = MutableStateFlow<Map<Long, String?>>(emptyMap())
    val moodMap: StateFlow<Map<Long, String?>> = _moodMap.asStateFlow()

    private val _confidenceMap     = MutableStateFlow<Map<Long, Float?>>(emptyMap())
    val confidenceMap: StateFlow<Map<Long, Float?>> = _confidenceMap.asStateFlow()

    private val _stressMap         = MutableStateFlow<Map<Long, Int?>>(emptyMap())
    val stressMap: StateFlow<Map<Long, Int?>> = _stressMap.asStateFlow()

    private val _emotionMap        = MutableStateFlow<Map<Long, String?>>(emptyMap())
    val emotionMap: StateFlow<Map<Long, String?>> = _emotionMap.asStateFlow()

    /* --- optional extras kept as single values --- */
    private val _summary            = MutableStateFlow<String?>(null)
    val summary: StateFlow<String?> = _summary.asStateFlow()

    private val _tone            = MutableStateFlow<String?>(null)
    val tone: StateFlow<String?> = _tone.asStateFlow()

    private val _reflectionQuestions            = MutableStateFlow<String?>(null)
    val reflectionQuestions: StateFlow<String?> = _reflectionQuestions.asStateFlow()

    private val _writingStyle            = MutableStateFlow<String?>(null)
    val writingStyle: StateFlow<String?> = _writingStyle.asStateFlow()

    /* One-shot events */
    private val _events = MutableSharedFlow<DiaryUiEvent>()
    val events: SharedFlow<DiaryUiEvent> = _events.asSharedFlow()

    init {
        observeEntries()
        // Initialize first-time user if needed
        viewModelScope.launch {
            userViewModel.createFirstTimeUser()
        }
    }

    //just for awarness and repeating to myself, suspend allows to perform a long running operation
    // without blocking the current thread
    suspend fun createEntry(text: String) {
        viewModelScope.launch(dispatchers.io) {
            //Change a mutable state variable isLoading to true
            setLoading(true)
        }
        runCatching {
            /* 1. Insert entry */
            Log.d("DiaryViewModel", "Inserting diary entry: $text")
            val entryId = diaryDao.insert(DiaryEntry(text = text, isDeleted = false))
            Log.d("DiaryViewModel", "Inserted entry with ID: $entryId")

            //Ask LLM to analyse Text


            val raw = gemmaClient.analyzeText(text)
            Log.d("Gemma", "Raw LLM response: $raw")

            //Parse the json response
            val parsed = GemmaParser.parse(raw, entryId)
            parsed?.let {
                diaryDao.insertAnalysis(it.analysis)
            }
            Log.d("Gemma", "Parsed analysis: ${parsed?.analysis}")

            // 2. Run separate tag generation (if needed)
            // val rawTags = gemmaClient.generateDiaryEntryTags(text) <-  avoid calling this now

            //We want the database entry to be one atomic operation
            val finalEntryId = database.withTransaction {
                val dvmId = diaryDao.insert(DiaryEntry(text = text, isDeleted = false))
                parsed?.analysis?.let { diaryDao.insertAnalysis(it.copy(entryId = dvmId)) }
                parsed?.tags?.forEach { tag ->
                    diaryDao.insertTag(Tag(entryId = dvmId, name = tag))
                }
                dvmId // Return the entry ID
            }
            
            // Asynchronously generate and update user emotional signature
            // This runs in the background and doesn't block the UI
            generateUserEmotionalSignatureAsync(text, finalEntryId)
        }.onFailure { throwable ->
            _events.emit(DiaryUiEvent.ShowError(throwable.message ?: "Unknown error"))
        }
        setLoading(false)
    }

    //Thumb of rule, when updating MutableStateFlow, always update like this
    // __stateFlow = newValue

    suspend fun searchThroughMemories(text: String) : String
    {
        val rawTags = gemmaClient.generateDiaryEntryTags(text)
        //LLM will generate 3 tags, we need to return one entry from these 3 tags
        val parsedTags = GemmaParser.parseTagsArray(rawTags)
        Log.d("DiaryViewModel", "Parsed tags: $parsedTags")

        val allMatches = mutableSetOf<DiaryEntry>()
        if (parsedTags != null) {
            for (tag in parsedTags) {
                Log.d("DiaryViewModel", "Searching for tag: $tag")
                allMatches.addAll(diaryDao.searchEntryUsingTag(tag))
            }
        }

        //we need to pick entries which are closet by the time stamp
        val bestMatch = allMatches.maxByOrNull { it.createdAt } // return a random entry or just NULL

        return bestMatch?.text ?: "No relevant memory found."
    }

    fun loadMood(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val result = diaryDao.getMoodForEntry(entryId)      // String?
                _mood.value = result                                // keep old API happy
                _moodMap.update { it + (entryId to result) }        // chart data
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading mood: ${e.message}"))
            }
        }
    }

    fun loadSummary(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val result = diaryDao.getSummaryForEntry(entryId)
                _summary.value = result
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading summary: ${e.message}"))
            }
        }
    }

    fun loadMoodConfidence(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val result = diaryDao.getMoodConfidence(entryId)    // Float?
                _moodConfidence.value = result
                _confidenceMap.update { it + (entryId to result) }
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading confidence: ${e.message}"))
            }
        }
    }

    fun loadStressLevel(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val result = diaryDao.getStressLevel(entryId)       // Int?
                _stressLevel.value = result
                _stressMap.update { it + (entryId to result) }
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading stress level: ${e.message}"))
            }
        }
    }

    fun loadTone(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                _tone.value = diaryDao.getTone(entryId)
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading tone: ${e.message}"))
            }
        }
    }



    fun loadEmotionDistribution(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val result = diaryDao.getEmotionDistribution(entryId) // String?
                _emotionDistribution.value = result
                _emotionMap.update { it + (entryId to result) }
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading emotion distribution: ${e.message}"))
            }
        }
    }

    fun loadReflectionQuestions(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                _reflectionQuestions.value = diaryDao.getReflectionQuestions(entryId)
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading reflection questions: ${e.message}"))
            }
        }
    }

    fun loadWritingStyle(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                _writingStyle.value = diaryDao.getWritingStyle(entryId)
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading writing style: ${e.message}"))
            }
        }
    }

    /* Internal Helper */
    private fun observeEntries() {
        diaryDao.getAllDiaryWithTags()                       // Flow<List<DiaryWithTags>>
            .onStart { setLoading(true) }
            .onEach { list -> _uiState.update { it.copy(entries = list, isLoading = false) } }
            .catch { e -> _events.emit(DiaryUiEvent.ShowError(e.message ?: "Unknown error")) }
            .flowOn(dispatchers.io)
            .launchIn(viewModelScope)
    }
    private fun setLoading(flag: Boolean) {
        _uiState.update { it.copy(isLoading = flag) }
    }


    /**
     * Asynchronously generates and updates user emotional signature
     * This doesn't block the main UI thread and runs in the background
     */
    private fun generateUserEmotionalSignatureAsync(entryText: String, entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                Log.d("DiaryViewModel", "Starting async user emotional signature generation for entry: $entryId")

                // Generate emotional signature using LLM
                val rawSignature = gemmaClient.generateUserEmotionalSignature(entryText)
                Log.d("DiaryViewModel", "Raw emotional signature response: $rawSignature")

                // Parse the response
                val parsedSignature = GemmaParser.parseUserSignatureJson(rawSignature)
                Log.d("DiaryViewModel", "Parsed emotional signature: $parsedSignature")

                // Update user profile if parsing was successful
                parsedSignature?.let { signature ->
                    updateUserProfileFromSignature(signature)
                }

            } catch (e: Exception) {
                Log.e("DiaryViewModel", "Failed to generate user emotional signature: ${e.message}", e)
                // Don't emit error to UI since this is background operation
            }
        }
    }

    /**
     * Updates user profile fields based on the parsed emotional signature
     */
    private suspend fun updateUserProfileFromSignature(signature: UserEntity) {
        try {
            // Update each field individually if it's not null
            signature.visualMoodColour?.let { color ->
                userViewModel.updateMoodColor(color)
                Log.d("DiaryViewModel", "Updated mood color: $color")
            }

            signature.moodSensitivityLevel?.let { level ->
                userViewModel.updateMoodSensitivityLevel(level)
                Log.d("DiaryViewModel", "Updated mood sensitivity level: $level")
            }

            signature.thinkingStyle?.let { style ->
                userViewModel.updateThinkingStyle(style)
                Log.d("DiaryViewModel", "Updated thinking style: $style")
            }

            signature.learningStyle?.let { style ->
                userViewModel.updateLearningStyle(style)
                Log.d("DiaryViewModel", "Updated learning style: $style")
            }

            signature.writingStyle?.let { style ->
                userViewModel.updateWritingStyle(style)
                Log.d("DiaryViewModel", "Updated writing style: $style")
            }

            signature.emotionalStrength?.let { strength ->
                userViewModel.updateEmotionalStrength(strength)
                Log.d("DiaryViewModel", "Updated emotional strength: $strength")
            }

            signature.emotionalWeakness?.let { weakness ->
                userViewModel.updateEmotionalWeakness(weakness)
                Log.d("DiaryViewModel", "Updated emotional weakness: $weakness")
            }

            signature.emotionalSignature?.let { emotionalSig ->
                userViewModel.updateEmotionalSignature(emotionalSig)
                Log.d("DiaryViewModel", "Updated emotional signature: $emotionalSig")
            }

        } catch (e: Exception) {
            Log.e("DiaryViewModel", "Failed to update user profile from signature: ${e.message}", e)
        }
    }
}

