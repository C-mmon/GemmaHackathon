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
import com.example.gemmahackathon.data.diary.DiaryEntry
import com.example.gemmahackathon.data.diary.Tag
import com.example.gemmahackathon.data.user.UserEntity
import com.example.gemmahackathon.domain.Logic.GemmaClient
import com.example.gemmahackathon.domain.Logic.GemmaParser
import com.example.gemmahackathon.ui.navigation.DiaryNavigation
import com.example.gemmahackathon.viewModel.DiaryViewModel
import com.example.gemmahackathon.viewModel.UserViewModel
import com.example.gemmahackathon.viewModel.UserViewModelFactory



class MainActivity : ComponentActivity() {
//saved instance small key value map that android passess to you only
//when the activity is being created for the first time
//if during resize there is a change in orientation., then bundle store the data
// whatever you held earlier
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    val db = DiaryDatabase.getDatabase(this@MainActivity)
    val dao = db.diaryDao()
    val userDao = db.userDao()

    //For now, keeping this here,
    val userViewModel = UserViewModel(userDao)

    lifecycleScope.launch {
        val gemma = GemmaClient(this@MainActivity)
        gemma.initialize()
        val diaryViewModel = DiaryViewModel(dao, gemma, userViewModel,db)
        setContent {
            GemmaHackathonTheme {
                DiaryNavigation(
                    diaryViewModel = diaryViewModel,
                    userViewModel = userViewModel
                )
            }
        }

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

// Removed TestDiaryView - replaced with new navigation structure


