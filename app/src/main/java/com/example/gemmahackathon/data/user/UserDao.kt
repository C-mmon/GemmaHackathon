package com.example.gemmahackathon.data.user

import androidx.room.*

@Dao
interface UserDao {
    //Insert or replace a user profile
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    // Fetch the user profile (we are in single-user scenario)
    @Query("SELECT * FROM UserProfile LIMIT 1")
    suspend fun getUser(): UserEntity?

    // Update the existing profile
    @Update
    suspend fun update(user: UserEntity)

}