package com.example.gemmahackathon.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gemmahackathon.data.DiaryDao
import com.example.gemmahackathon.data.DiaryEntry

//class will have one table Diary Entry
//version numbers helps for the migration strategy
@Database(entities = [DiaryEntry::class, Tag::class, DiaryAnalysis::class], version = 2)
//annotation marks the class as a room data ase
//abstract class that extends Room Database
abstract class DiaryDatabase : RoomDatabase() {
    //abstract function that returns the DiaryDao
    abstract fun diaryDao(): DiaryDao
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