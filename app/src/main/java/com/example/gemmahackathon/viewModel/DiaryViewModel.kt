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

    //state
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

    private val _mood = MutableStateFlow<String?>(null)
    val mood: StateFlow<String?> = _mood.asStateFlow()

    private val _moodConfidence = MutableStateFlow<Float?>(null)
    val moodConfidence: StateFlow<Float?> = _moodConfidence.asStateFlow()

    private val _summary = MutableStateFlow<String?>(null)
    val summary: StateFlow<String?> = _summary.asStateFlow()

    private val _tone = MutableStateFlow<String?>(null)
    val tone: StateFlow<String?> = _tone.asStateFlow()

    private val _stressLevel = MutableStateFlow<Int?>(null)
    val stressLevel: StateFlow<Int?> = _stressLevel.asStateFlow()

    private val _emotionDistribution = MutableStateFlow<String?>(null)
    val emotionDistribution: StateFlow<String?> = _emotionDistribution.asStateFlow()

    private val _reflectionQuestions = MutableStateFlow<String?>(null)
    val reflectionQuestions: StateFlow<String?> = _reflectionQuestions.asStateFlow()

    private val _writingStyle = MutableStateFlow<String?>(null)
    val writingStyle: StateFlow<String?> = _writingStyle.asStateFlow()

    /* One-shot events */
    private val _events = MutableSharedFlow<DiaryUiEvent>()
    val events: SharedFlow<DiaryUiEvent> = _events.asSharedFlow()

    init {
        observeEntries()
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
            database.withTransaction {
                val dvmId = diaryDao.insert(DiaryEntry(text = text, isDeleted = false))
                parsed?.analysis?.let { diaryDao.insertAnalysis(it.copy(entryId = dvmId)) }
                parsed?.tags?.forEach { tag ->
                    diaryDao.insertTag(Tag(entryId = dvmId, name = tag))
                }
            }
            //For now, we want to allow each entry update existing user profile.
            //But in future support, we want to
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
                val result = diaryDao.getMoodForEntry(entryId)
                _mood.value = result
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
                val result = diaryDao.getMoodConfidence(entryId)
                _moodConfidence.value = result
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading summary: ${e.message}"))
            }
        }
    }

    fun getStressLevel(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                val result = diaryDao.getMoodConfidence(entryId)
                _moodConfidence.value = result
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading summary: ${e.message}"))
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

    fun loadStressLevel(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                _stressLevel.value = diaryDao.getStressLevel(entryId)
            } catch (e: Exception) {
                _events.emit(DiaryUiEvent.ShowError("Error loading stress level: ${e.message}"))
            }
        }
    }

    fun loadEmotionDistribution(entryId: Long) {
        viewModelScope.launch(dispatchers.io) {
            try {
                _emotionDistribution.value = diaryDao.getEmotionDistribution(entryId)
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


    //We need
}

