package com.example.gemmahackathon.data.diary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateMillis: Long = System.currentTimeMillis(),
    val text: String,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val entryColor: String? = null //Each unique entry will have a colour associated to it, suggested by gemma

    //Room Does not store list directly
)
