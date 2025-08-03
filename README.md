//Check list for production
-> Catch LLM failures, nulls and parse exception
-> Voice/image upload to be added, 
-> Security model file handling (no leaking on disk)
-> Optional: Biometric lock for app
-> Local data encryption if storing sensitive info
-> Offload LLM inference to background thread

-> UI/UX readiness
-> Time to remove all logic from mainActivity and use viewModel for all stateful logic

->Important:
Use this pattern in ViewModel avoid analysing the same text again, enforce this always, regardless of where create entry is called.

```
val existing = dao.getDiaryWithAnalysis(entryId)
if (existing?.analysis == null) {
    val reply = gemma.analyzeText(text)
    val result = GemmaParser.parse(reply, entryId)

    if (result != null) {
        dao.insertAnalysis(result.analysis)
        result.tags.forEach { tag ->
            dao.insertTag(Tag(entryId = entryId, name = tag))
        }
    }
}
```

//Gemma analyze should run withContext to allow LLM run off the UI thread, and UI stays responsive
//We need to run this code block on the default dispatcher (bg thread) thenn return the result
suspend fun analyzeText(prompt: String): String? = withContext(Dispatchers.Default) {
    llm?.generateResponse(prompt)
}

//Somewhere in the view model, you would have something like this.
viewModelScope.launch {
    val reply = gemma.analyzeText(...)
}

//Calling this with,val reply = gemma.analyzeText(...) // directly in onCreate() without coroutine, will cause UI to freeze

//Need to handle the logic for update text, currently I dont want to update the view model
// I rather update the entry text, delete old analyzie and tags, run llm again 
```
fun updateDiaryEntry(entryId: Long, newText: String) {
    viewModelScope.launch {
        // 1. Update the entry text
        val updatedEntry = DiaryEntry(id = entryId, text = newText, isDeleted = false)
        dao.update(updatedEntry)

        // 2. Delete old analysis & tags
        dao.deleteAnalysisByEntryId(entryId)
        dao.deleteTagsByEntryId(entryId)

        // 3. Run LLM again
        val reply = gemma.analyzeText(newText)
        val parsed = GemmaParser.parse(reply, entryId)

        if (parsed != null) {
            dao.insertAnalysis(parsed.analysis)
            parsed.tags.forEach { tag ->
                dao.insertTag(Tag(entryId = entryId, name = tag))
            }
        }

        loadAll() // refresh entries
    }
}
```

Add this to dao function
```
@Query("DELETE FROM DiaryAnalysis WHERE entryId = :entryId")
suspend fun deleteAnalysisByEntryId(entryId: Long)

@Query("DELETE FROM tags WHERE entryId = :entryId")
suspend fun deleteTagsByEntryId(entryId: Long)

@Update
suspend fun update(entry: DiaryEntry)
```

//Need to suggest mindfulness activity, 

//Emotional Signature,
//Highlight user important strength based on all past diary entry
//Emotional summary for the week
//Emoji for mood of each day
//Visual Mood map (color of entry will change based on mood), size is tress,
//Entry Linkings based on summary 
//Mood goal alignment check, 
//Allow user to set the mood and then check if the entries are alginign as per that or not
//Personal growth tracker,
//Future Reflection Prediction
