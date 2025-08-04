package com.example.gemmahackathon.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemmahackathon.data.diary.DiaryDao
import com.example.gemmahackathon.data.diary.Tag
import com.example.gemmahackathon.domain.Logic.GemmaParser
import com.example.gemmahackathon.data.diary.DiaryEntry
import com.example.gemmahackathon.data.diary.DiaryWithTags
import com.example.gemmahackathon.domain.Logic.GemmaClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import android.util.Log


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
    private val dispatchers: DispatcherProvider = DefaultDispatchers
) : ViewModel() {

    //state
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()

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
            val rawTags = gemmaClient.generateDiaryEntryTags(text)
            Log.d("Gemma", "Raw LLM tag response: $rawTags")
            val tagOnlyResult = GemmaParser.parseTagsArray(rawTags)

            tagOnlyResult?.forEach { tag ->
                Log.d("Gemma", "Parsed tag (from tag generator): $tag")
                diaryDao.insertTag(Tag(entryId = entryId, name = tag))
            }

        }.onFailure { throwable ->
            _events.emit(DiaryUiEvent.ShowError(throwable.message ?: "Unknown error"))
        }

        setLoading(false)
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

