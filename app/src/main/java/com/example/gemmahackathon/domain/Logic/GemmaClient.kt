package com.example.gemmahackathon.domain.Logic

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import android.content.Context

import android.util.Log          // for Log.i / Log.e
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File              // for File()

class GemmaClient(private val context: Context)
{
    private var llm: LlmInference? = null

    //Anything needing the class level scope comes here
    //it is basically a singleton object here
    companion object
    {
        private const val TAG ="GemmaClient"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"

    }


    //kotlin restricts susepend function like withContext to only be used insidde another susepend function
    suspend fun initialize() {
        val path = locateModelFile() ?: throw IllegalStateException("Gemma model not found")
        Log.i(TAG, "Using model at: $path")

        //For production: Add support to avoid running in llm mode.

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(path)
            .build()

        withContext(Dispatchers.Default) {
            llm = LlmInference.createFromOptions(context, options)
        }
    }

    //Updating to return null
    suspend fun analyzeText(entryText: String): String? {
        val prompt = """
            Analyze the following diary entry, if the diary entry is short, return NULL,and return a JSON with:
            - mood, moodConfidence, summary, reflectionQuestions, writingStyle, emotionDistribution, stressLevel, tone
            Entry:
            "$entryText"
        """.trimIndent()

        return withContext(Dispatchers.Default) {
            llm?.generateResponse(prompt)
        }
    }

    suspend fun generateDiaryEntryTags(entryText: String): String? {
        val prompt = """
            Analyze the following diary entry and return a JSON with only 3 tags entries that are appropriate for the give:
            - tags
            Entry:
            "$entryText"
        """.trimIndent()

        return withContext(Dispatchers.Default) {
            llm?.generateResponse(prompt)
        }
    }


    fun close() {
        llm?.close()
    }

    private fun locateModelFile(): String? {
        val internal = File(context.filesDir, MODEL_FILENAME)
        if (internal.exists()) return internal.absolutePath

        val external = File(context.getExternalFilesDir(null), MODEL_FILENAME)
        if (external.exists()) return external.absolutePath

        Log.e(TAG, "Gemma model not found.")
        return null
    }

}