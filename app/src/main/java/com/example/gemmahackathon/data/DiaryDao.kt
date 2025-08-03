package com.example.gemmahackathon.data

import androidx.room.*

@Dao
interface DiaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntry): Long

    @Update
    suspend fun update(entry: DiaryEntry)

    @Delete
    suspend fun delete(entry: DiaryEntry)

    @Query("SELECT * FROM entries WHERE isDeleted = 0 ORDER BY dateMillis DESC")
    suspend fun getAllEntries(): List<DiaryEntry>

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): DiaryEntry?

    @Query("UPDATE entries SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    //Tag related Operation
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<Tag>)

    @Query("DELETE FROM tags WHERE entryId = :entryId")
    suspend fun deleteTagsForEntry(entryId: Long)

    // DiaryWithTags relation queries
    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getDiaryWithTags(id: Long): DiaryWithTags?

    @Transaction
    @Query("SELECT * FROM entries WHERE isDeleted = 0 ORDER BY dateMillis DESC")
    suspend fun getAllDiaryWithTags(): List<DiaryWithTags>

    // DiaryAnalysis operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysis(analysis: DiaryAnalysis)

    @Update
    suspend fun updateAnalysis(analysis: DiaryAnalysis)

    @Delete
    suspend fun deleteAnalysis(analysis: DiaryAnalysis)

    @Query("SELECT * FROM DiaryAnalysis WHERE entryId = :entryId LIMIT 1")
    suspend fun getAnalysisForEntry(entryId: Long): DiaryAnalysis?

    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getDiaryWithAnalysis(id: Long): DiaryWithAnalysis?
}