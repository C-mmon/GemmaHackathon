package com.example.gemmahackathon.domain.Logic

import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.vision.imageclassifier.ImageClassifier
import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
class GemmaClient(private val context: Context)
{
    private var llm: LlmInference? = null

    //Anything needing the class level scope comes here
    //it is basically a singleton object here
    companion object
    {
        private const val TAG ="GemmaClient"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
        const val DEFAULT_MAX_TOKENS = 1000
        const val DEFAULT_TOP_K = 0

    }

    //kotlin restricts susepend function like withContext to only be used insidde another susepend function
    suspend fun initialize() {
        val path = locateModelFile() ?: throw IllegalStateException("Gemma model not found")
        Log.i(TAG, "Using model at: $path")

        //For production: Add support to avoid running in llm mode.
        val options =
            LlmInference.LlmInferenceOptions.builder()
                .setModelPath(path)
                .setMaxTokens(DEFAULT_MAX_TOKENS)
                .setMaxTopK(DEFAULT_TOP_K)
                .build()

        withContext(Dispatchers.Default) {
            llm = LlmInference.createFromOptions(context, options)
        }
    }

    //Updating to return null
    // So for now, let analyze text and return tags as well,
    //avoid calling llm too much
    suspend fun analyzeText(entryText: String): String? {
        val prompt = """
            Analyze the following diary entry and return a JSON with these exact fields:
            - mood: string (positive/negative/neutral)
            - moodConfidence: number (0.0 to 1.0)
            - summary: string (brief summary)
            - reflectionQuestions: string (1-2 questions)
            - writingStyle: string (brief description)
            - emotionDistribution: object (e.g., {"joy": 0.8, "peace": 0.2})
            - stressLevel: number (0-10 integer)
            - tone: string (brief description)
            - self-help: string (brief coping suggestion)
            - tags: array of 3 strings maximum
            
            Return only valid JSON. For stressLevel use integers 0-10. For self-help, provide a concise suggestion.
            
            Entry: "$entryText"
        """.trimIndent()

        return withContext(Dispatchers.Default) {
            llm?.generateResponse(prompt)
        }
    }

    //Future Feature: if in case, we want to integrate notes building, then we can call this section
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


    suspend fun generateUserEmotionalSignature(entryText: String): String? {
        val prompt = """
            Analyze the following diary entry and return a JSON with:
            - visualMoodColour, moodSensitivityLevel, thinkingStyle, learningStyle, writingStyle, emotionalStrength, emotionalWeakness, emotionalSignature
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