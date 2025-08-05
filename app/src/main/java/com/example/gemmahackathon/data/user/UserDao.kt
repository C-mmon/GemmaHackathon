package com.example.gemmahackathon.data.user

import androidx.room.*
import kotlinx.coroutines.flow.*

@Dao
interface UserDao {
    //Insert or replace a user profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM UserProfile LIMIT 1")
    fun getUserFlow(): Flow<UserEntity?> //Again declaring this as a Flow, because we want dynamic update

    // Update the existing profile
    @Update
    suspend fun update(user: UserEntity)

}