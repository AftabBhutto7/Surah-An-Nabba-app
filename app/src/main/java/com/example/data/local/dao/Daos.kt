package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HifzProgressEntity
import com.example.data.local.entities.JournalEntity
import com.example.data.local.entities.QuizRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getBookmarkByAyah(ayahNumber: Int): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Delete
    suspend fun deleteBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE ayahNumber = :ayahNumber")
    suspend fun deleteByAyah(ayahNumber: Int)
}

@Dao
interface HifzDao {
    @Query("SELECT * FROM hifz_progress ORDER BY ayahNumber ASC")
    fun getAllProgress(): Flow<List<HifzProgressEntity>>

    @Query("SELECT * FROM hifz_progress WHERE ayahNumber = :ayahNumber LIMIT 1")
    suspend fun getProgressForAyah(ayahNumber: Int): HifzProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: HifzProgressEntity)

    @Query("UPDATE hifz_progress SET status = :newStatus, reviewCount = reviewCount + 1, lastTestedTimestamp = :now WHERE ayahNumber = :ayahNumber")
    suspend fun updateStatus(ayahNumber: Int, newStatus: String, now: Long = System.currentTimeMillis())
}

@Dao
interface QuizRecordDao {
    @Query("SELECT * FROM quiz_records ORDER BY timestamp DESC LIMIT 50")
    fun getRecentQuizzes(): Flow<List<QuizRecordEntity>>

    @Insert
    suspend fun insertQuizRecord(record: QuizRecordEntity)
}

@Dao
interface JournalDao {
    @Query("SELECT * FROM reflections_journal ORDER BY timestamp DESC")
    fun getAllReflections(): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReflection(journal: JournalEntity)

    @Delete
    suspend fun deleteReflection(journal: JournalEntity)
}
