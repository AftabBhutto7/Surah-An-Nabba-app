package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val ayahNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String? = null,
    val colorTag: String = "#1B4332"
)

@Entity(tableName = "hifz_progress")
data class HifzProgressEntity(
    @PrimaryKey val ayahNumber: Int,
    val status: String, // "NOT_STARTED", "LEARNING", "MEMORIZED", "MASTERED"
    val reviewCount: Int = 0,
    val lastTestedTimestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(tableName = "quiz_records")
data class QuizRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizType: String, // "VOCABULARY", "ROOT_MATCH", "AYAH_ORDER", "TAJWEED"
    val score: Int,
    val totalQuestions: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reflections_journal")
data class JournalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ayahNumber: Int,
    val reflectionText: String,
    val timestamp: Long = System.currentTimeMillis()
)
