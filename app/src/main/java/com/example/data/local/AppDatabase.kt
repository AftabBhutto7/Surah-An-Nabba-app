package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookmarkDao
import com.example.data.local.dao.HifzDao
import com.example.data.local.dao.JournalDao
import com.example.data.local.dao.QuizRecordDao
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HifzProgressEntity
import com.example.data.local.entities.JournalEntity
import com.example.data.local.entities.QuizRecordEntity

@Database(
    entities = [
        BookmarkEntity::class,
        HifzProgressEntity::class,
        QuizRecordEntity::class,
        JournalEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun hifzDao(): HifzDao
    abstract fun quizRecordDao(): QuizRecordDao
    abstract fun journalDao(): JournalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "surah_naba_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
