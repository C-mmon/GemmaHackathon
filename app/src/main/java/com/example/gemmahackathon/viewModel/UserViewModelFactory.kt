package com.example.gemmahackathon.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.gemmahackathon.data.user.UserDao

class UserViewModelFactory(
    private val userDao: UserDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(userDao) as T
            else -> throw IllegalArgumentException("UserViewModelFactory can only create UserViewModel")
        }
}