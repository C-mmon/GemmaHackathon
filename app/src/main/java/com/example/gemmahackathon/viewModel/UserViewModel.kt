package com.example.gemmahackathon.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gemmahackathon.data.user.UserDao
import com.example.gemmahackathon.data.user.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserViewModel @Inject constructor(
    private val userDao: UserDao
) : ViewModel() {

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user.asStateFlow()

    init {
        viewModelScope.launch {
            userDao.getUserFlow()
                .collectLatest { _user.value = it }
        }
    }

    fun updateMoodColor(colorHex: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(visualMoodColour = colorHex)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateUserName(newName: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(name = newName)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateAbout(newAbout: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(about = newAbout)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateMoodSensitivityLevel(level: Int) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(moodSensitivityLevel = level)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateThinkingStyle(style: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(thinkingStyle = style)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateLearningStyle(style: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(learningStyle = style)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateWritingStyle(style: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(writingStyle = style)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateEmotionalStrength(strength: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(emotionalStrength = strength)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateEmotionalWeakness(weakness: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(emotionalWeakness = weakness)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }

    fun updateEmotionalSignature(signature: String) {
        viewModelScope.launch {
            _user.value?.let {
                val updated = it.copy(emotionalSignature = signature)
                userDao.update(updated)
                _user.value = updated
            }
        }
    }
}
