package com.example.gemmahackathon.data

import androidx.room.Embedded
import androidx.room.Relation
import com.example.gemmahackathon.data.DiaryEntry
import com.example.gemmahackathon.data.DiaryAnalysis

data class DiaryWithAnalysis(
    @Embedded val diary: DiaryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val analysis: DiaryAnalysis?
)