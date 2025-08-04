package com.example.gemmahackathon.data.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserProfile")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    //Main Component
    val name: String,
    val about:  String,

    //Something about the user itself
    val visualMoodColour: String?= null,
    val moodSensitivityLevel: Int?= null,
    val thinkingStyle: String ?= null,
    val learningStyle: String?= null,
    val writingStyle: String?= null ,
    val emotionalStrength: String ?= null,
    val emotionalWeakness: String ?= null,
    val emotionalSignature: String ?= null
)

