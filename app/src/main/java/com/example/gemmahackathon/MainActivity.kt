package com.example.gemmahackathon


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gemmahackathon.ui.theme.GemmaHackathonTheme
import androidx.lifecycle.lifecycleScope
import android.util.Log // For Log
import com.google.mediapipe.tasks.genai.llminference.LlmInference // For LlmInference
import kotlinx.coroutines.Dispatchers // For Dispatchers
import kotlinx.coroutines.withContext // For withContext
import java.io.File // For File
import kotlinx.coroutines.launch
import com.example.gemmahackathon.data.DiaryDatabase
import com.example.gemmahackathon.data.DiaryEntry
import com.example.gemmahackathon.data.*
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    companion object
    {
        private const val TAG = "GemmaLoader"
        private const val MODEL_FILENAME = "gemma-3n-E2B-it-int4.task"
    }
    private var llm: LlmInference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { androidx.compose.material3.Text("Loading Gemma…") }

        lifecycleScope.launchWhenStarted {
            val modelPath = locateModelFile() ?: return@launchWhenStarted
            Log.i(TAG, "Using model at: $modelPath")

            val options = LlmInference.LlmInferenceOptions.builder()
                .setModelPath(modelPath)          // ABSOLUTE path!
                .build()

            // Initialisation & inference off UI thread
            withContext(Dispatchers.Default) {
                llm = LlmInference.createFromOptions(applicationContext, options)
            }

            val db = DiaryDatabase.getDatabase(this@MainActivity)
            val dao = db.diaryDao()

            lifecycleScope.launch {
                // 1. Insert dummy DiaryEntry
                val entry = DiaryEntry(
                    text = "Today I felt a bit anxious but hopeful.",
                    isDeleted = false
                )

                val entryId = dao.insert(entry)

                val insertedEntry = dao.getEntryById(entryId)
                val prompt = """
                    Analyze the following diary entry and return a JSON with the following fields:
                    - mood (string)
                    - moodConfidence (float)
                    - summary (string)
                    - reflectionQuestions (array of strings)
                    - writingStyle (string)
                    - emotionDistribution (map of emotion:string to float)
                    - stressLevel (integer from 1 to 10)
                    - tone (string)
                    Entry:
                    "${insertedEntry?.text}"
                    Response format:
                    {
                    "mood": "...",
                    "moodConfidence": ...,
                    ...
                    }
                    """.trimIndent()
                var reply = withContext(Dispatchers.Default) {
                    llm?.generateResponse(prompt)
                }
                Log.i(TAG, "Gemma reply: $reply")

                try{
                    val cleanJson = reply
                        ?.replace("```json","")
                        ?.replace("```","")
                        ?.trim()
                    Log.i(TAG, "Cleaned JSON: $cleanJson")
                    val json = JSONObject(cleanJson)

                    val analysis = DiaryAnalysis(
                        entryId = entryId,
                        mood = json.optString("mood"),
                        moodConfidence = json.optDouble("moodConfidence").toFloat(),
                        summary = json.optString("summary"),
                        reflectionQuestions = json.optJSONArray("reflectionQuestions")
                            ?.let { (0 until it.length()).joinToString(", ") { i -> it.getString(i) } },
                        writingStyle = json.optString("writingStyle"),
                        emotionDistribution = json.optJSONObject("emotionDistribution")?.toString(),
                        stressLevel = json.optInt("stressLevel"),
                        tone = json.optString("tone")
                    )
                    dao.insertAnalysis(analysis)
                }
                catch (e: Exception) {
                    Log.e(TAG, "Error parsing JSON: ${e.message}")
                }
                // 7. Optional: fetch & verify result
                val result = dao.getDiaryWithAnalysis(entryId)
                result?.let {
                    Log.d("TestResult", "Text: ${it.diary.text}")
                    Log.d("TestResult", "Mood: ${it.analysis?.mood}")
                    Log.d("TestResult", "Summary: ${it.analysis?.summary}")
                    Log.d("TestResult", "Emotion: ${it.analysis?.emotionDistribution}")
                }
            }


        }
    }

    /**
     * Looks for the model in places your app can always read.
     * 1) Internal storage   /data/user/0/<pkg>/files
     * 2) External sandbox   /sdcard/Android/data/<pkg>/files  (push with ADB)
     * Returns absolute path, or null if not found.
     */
    private fun locateModelFile(): String? {
        // 1. Internal private dir (best place if you download the file yourself)
        val internal = File(filesDir, MODEL_FILENAME)
        if (internal.exists()) return internal.absolutePath

        // 2. External app-private sandbox (works with a single ADB push)
        val external = File(getExternalFilesDir(null), MODEL_FILENAME)
        if (external.exists()) return external.absolutePath

        // 3. Not found – print clear instructions and bail
        Log.e(
            TAG, """
            Gemma model not found.
            Push it with:
            adb push $MODEL_FILENAME "/sdcard/Android/data/$packageName/files/"
            """.trimIndent()
        )
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        llm?.close()
    }

}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GemmaHackathonTheme {
        Greeting("Android")
    }
}


