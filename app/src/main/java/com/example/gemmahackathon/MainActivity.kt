package com.example.gemmahackathon


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.gemmahackathon.ui.theme.GemmaHackathonTheme
import androidx.lifecycle.lifecycleScope
import android.util.Log // For Log
import kotlinx.coroutines.launch
import com.example.gemmahackathon.data.DiaryDatabase
import com.example.gemmahackathon.data.DiaryEntry
import com.example.gemmahackathon.data.Tag
import com.example.gemmahackathon.domain.Logic.GemmaClient
import com.example.gemmahackathon.domain.Logic.GemmaParser



class MainActivity : ComponentActivity() {
//saved instance small key value map that android passess to you only
//when the activity is being created for the first time
//if during resize there is a change in orientation., then bundle store the data
// whatever you held earlier
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Text("Loading Gemma…") }

        lifecycleScope.launch {
            val gemma = GemmaClient(this@MainActivity)
            gemma.initialize()

            val db = DiaryDatabase.getDatabase(this@MainActivity)
            val dao = db.diaryDao()

            //Temp solution: was getting multiple entries for the same time
            dao.clearAllTags()
            dao.clearAllAnalysis()
            dao.clearAllEntries()

            val entry = DiaryEntry(text = "Today I felt a bit anxious but hopeful.", isDeleted = false)
            val entryId = dao.insert(entry)
            //Insert tag for the given entry id
            dao.insertTag(Tag(entryId = entryId, name = "calm"))

            val insertedEntry = dao.getEntryById(entryId)
            val reply = gemma.analyzeText(insertedEntry?.text.orEmpty())

            val resultList = dao.getAllDiaryWithTags()
            resultList.forEach { item ->
                Log.d("Check", "Entry: ${item.diaryEntry.text}")
                Log.d("Check", "Tags: ${item.tags.joinToString { it.name }}")
            }


            val analysis = GemmaParser.parse(reply, entryId)
            val resultParsed = GemmaParser.parse(reply, entryId)
            if (resultParsed != null) {
                dao.insertAnalysis(resultParsed.analysis)
                resultParsed.tags.forEach { tag ->
                    dao.insertTag(Tag(entryId = entryId, name = tag))
                }
            }

            Log.d("TestResult", "Mood: ${resultParsed?.analysis?.mood}")
            Log.d("TestResult", "Summary: ${resultParsed?.analysis?.summary}")
            Log.d("TestResult", "Tone: ${resultParsed?.analysis?.tone}")
            Log.d("TestResult", "Questions: ${resultParsed?.analysis?.reflectionQuestions}")

        }
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


