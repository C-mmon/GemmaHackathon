package com.example.gemmahackathon.data.diary

import androidx.room.Embedded
import androidx.room.Relation

data class DiaryWithAnalysis(
    @Embedded val diary: DiaryEntry,
    @Relation(
        parentColumn = "id",
        entityColumn = "entryId"
    )
    val analysis: DiaryAnalysis?
)