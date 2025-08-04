package com.example.gemmahackathon.data.diary

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = DiaryEntry::class,
        parentColumns = ["id"],
        childColumns = ["entryId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices =  [Index("entryId")] //Create a database index on the entryId column to improve query performance
    //Helpful because we are going to frequently query or join on entry id
    //Select * from DiaryEntry where id = :id

)

data class DiaryAnalysis(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val entryId: Long,                    // Link to DiaryEntry

    val mood: String? = null,
    val moodConfidence: Float? = null,
    val summary: String? = null,
    val reflectionQuestions: String? = null, // JSON string or comma-separated
    val writingStyle: String? = null,
    val emotionDistribution: String? = null, // JSON string: {"Joy":0.6,"Anger":0.1}
    val stressLevel: Int? = null,
    val tone: String? = null

    //Future: Allow to store image and voice notes as part of the entry
    // Since gemma 3n does not support inference based on direct voice and image, this will be something left for the future to think of

    //Remembering my golden rules,
    // Ui knows view model only : No room or LLM logic in UI
    // view model knows ony use, keeps ui testable
    //use case knows repo +logic
    // repo knows room and gemma

)