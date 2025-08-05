package com.example.gemmahackathon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gemmahackathon.ui.theme.GemmaHackathonTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.gemmahackathon.data.DiaryDatabase
import com.example.gemmahackathon.domain.Logic.GemmaClient
import com.example.gemmahackathon.ui.navigation.DiaryNavigation
import com.example.gemmahackathon.viewModel.DiaryViewModel
import com.example.gemmahackathon.viewModel.UserViewModel



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


