package com.example.gemmahackathon.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gemmahackathon.data.diary.DiaryDao
import com.example.gemmahackathon.domain.Logic.GemmaClient

class DiaryViewModelFactory(
    private val diaryDao: DiaryDao,
    private val gemmaClient: GemmaClient,
    private val userViewModel: UserViewModel
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(DiaryViewModel::class.java) ->
                DiaryViewModel(diaryDao, gemmaClient, userViewModel) as T // Use the correct parameter

            else -> throw IllegalArgumentException(
                "DiaryViewModelFactory can only create DiaryViewModel"
            )
        }
}