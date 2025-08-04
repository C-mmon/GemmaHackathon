package com.example.gemmahackathon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gemmahackathon.data.diary.DiaryAnalysis
import com.example.gemmahackathon.data.diary.DiaryDao
import com.example.gemmahackathon.data.diary.DiaryEntry
import com.example.gemmahackathon.data.diary.Tag
import com.example.gemmahackathon.data.user.UserDao
import com.example.gemmahackathon.data.user.UserEntity

//class will have one table Diary Entry
//version numbers helps for the migration strategy
@Database(entities = [DiaryEntry::class, Tag::class, DiaryAnalysis::class, UserEntity::class], version = 3)
//annotation marks the class as a room data ase
//abstract class that extends Room Database
abstract class DiaryDatabase : RoomDatabase() {
    //abstract function that returns the DiaryDao
    abstract fun diaryDao(): DiaryDao
    abstract fun userDao(): UserDao //Adding a new accessor to the room db
    //companion object are similar to static in java/
    // proper
    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null

        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                ).fallbackToDestructiveMigration() //Wipes DB when version changes
                    .build()
                INSTANCE = instance
                instance

            }
        }
    }
}