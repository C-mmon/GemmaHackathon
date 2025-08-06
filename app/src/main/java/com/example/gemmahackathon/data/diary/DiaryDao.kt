package com.example.gemmahackathon.data.diary

import androidx.room.*
import kotlinx.coroutines.flow.*

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
    suspend fun insertTag(tags: List<Tag>)

    @Query("DELETE FROM tags WHERE entryId = :entryId")
    suspend fun deleteTagsForEntry(entryId: Long)

    @Query
        ("""
            SELECT * FROM entries  
            WHERE id IN (
            SELECT entryId FROM tags WHERE name LIKE '%' || :tag || '%')
        """)
    suspend fun searchEntryUsingTag(tag: String): List<DiaryEntry>

    // DiaryWithTags relation queries
    @Transaction
    @Query("SELECT * FROM entries WHERE id = :id")
    suspend fun getDiaryWithTags(id: Long): DiaryWithTags?

    @Transaction
    @Query("SELECT * FROM entries WHERE isDeleted = 0 ORDER BY dateMillis DESC")
    fun getAllDiaryWithTags(): Flow<List<DiaryWithTags>> //Flow<List<DiaryWithTags>>
    //Flow automatically emits a new list every time the data in the database changes

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: Tag)

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


    //Note: I have not tested this part, but to be tested
    //My idea is that LLM  will provide the mood capiability and post that we shall be able to search here
    //This provides an entire set of interface to interact with our diary analysis
    @Query("SELECT * FROM DiaryAnalysis where mood = :mood")
    suspend fun searchBySadMood(mood: String): List<DiaryAnalysis>

    @Query("SELECT mood FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getMoodForEntry(entryId: Long): String?

    @Query("SELECT moodConfidence FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getMoodConfidence(entryId: Long): Float?

    @Query("SELECT summary FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getSummaryForEntry(entryId: Long): String?

    @Query("SELECT reflectionQuestions FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getReflectionQuestions(entryId: Long): String?

    @Query("SELECT selfhelp FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getSelfHelp(entryId: Long): String?

    @Query("SELECT writingStyle FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getWritingStyle(entryId: Long): String?

    @Query("SELECT emotionDistribution FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getEmotionDistribution(entryId: Long): String?

    @Query("SELECT stressLevel FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getStressLevel(entryId: Long): Int?

    @Query("SELECT tone FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getTone(entryId: Long): String?

    @Query("SELECT selfhelp FROM DiaryAnalysis WHERE entryId = :entryId")
    suspend fun getSelfhelp(entryId: Long): String?

    //Delete functionality
    @Query("DELETE FROM entries")
    suspend fun clearAllEntries()

    @Query("DELETE FROM tags")
    suspend fun clearAllTags()

    @Query("DELETE FROM DiaryAnalysis")
    suspend fun clearAllAnalysis()

    //This feature needs to be supported, user editing its old entry will end up deleting entire diaryAnalysis
    //@Update
    //suspend fun update(entry: DiaryEntry)
}