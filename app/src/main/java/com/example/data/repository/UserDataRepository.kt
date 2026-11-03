package com.example.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.AppDatabase
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HifzProgressEntity
import com.example.data.local.entities.JournalEntity
import com.example.data.local.entities.QuizRecordEntity
import com.example.data.model.Reciter
import com.example.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class UserDataRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val bookmarkDao = db.bookmarkDao()
    private val hifzDao = db.hifzDao()
    private val quizDao = db.quizRecordDao()
    private val journalDao = db.journalDao()
    private val dataStore = context.dataStore

    // Keys
    private object PrefKeys {
        val LAST_READ_AYAH = intPreferencesKey("last_read_ayah")
        val ARABIC_FONT_SIZE = floatPreferencesKey("arabic_font_size")
        val TRANSLATION_FONT_SIZE = floatPreferencesKey("translation_font_size")
        val SHOW_TRANSLITERATION = booleanPreferencesKey("show_transliteration")
        val SHOW_URDU = booleanPreferencesKey("show_urdu")
        val SHOW_ENGLISH = booleanPreferencesKey("show_english")
        val SHOW_WORD_BY_WORD = booleanPreferencesKey("show_word_by_word")
        val RECITER_ID = stringPreferencesKey("reciter_id")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val DAILY_READING_GOAL = intPreferencesKey("daily_reading_goal")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val EYE_COMFORT_MODE = booleanPreferencesKey("eye_comfort_mode")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data.map { prefs ->
        val reciterId = prefs[PrefKeys.RECITER_ID] ?: Reciter.ALAFASY.id
        val reciter = Reciter.values().firstOrNull { it.id == reciterId } ?: Reciter.ALAFASY

        UserPreferences(
            arabicFontSize = prefs[PrefKeys.ARABIC_FONT_SIZE] ?: 28f,
            translationFontSize = prefs[PrefKeys.TRANSLATION_FONT_SIZE] ?: 16f,
            showTransliteration = prefs[PrefKeys.SHOW_TRANSLITERATION] ?: true,
            showUrduTranslation = prefs[PrefKeys.SHOW_URDU] ?: true,
            showEnglishTranslation = prefs[PrefKeys.SHOW_ENGLISH] ?: true,
            showWordByWord = prefs[PrefKeys.SHOW_WORD_BY_WORD] ?: false,
            selectedReciter = reciter,
            playbackSpeed = prefs[PrefKeys.PLAYBACK_SPEED] ?: 1.0f,
            dailyReadingGoalAyahs = prefs[PrefKeys.DAILY_READING_GOAL] ?: 10,
            darkTheme = prefs[PrefKeys.DARK_THEME] ?: false,
            eyeComfortMode = prefs[PrefKeys.EYE_COMFORT_MODE] ?: false,
            appLanguage = prefs[PrefKeys.APP_LANGUAGE] ?: "en"
        )
    }

    val lastReadAyahFlow: Flow<Int> = dataStore.data.map { prefs ->
        prefs[PrefKeys.LAST_READ_AYAH] ?: 1
    }

    suspend fun setLastReadAyah(ayahNumber: Int) {
        dataStore.edit { it[PrefKeys.LAST_READ_AYAH] = ayahNumber }
    }

    suspend fun updatePreferences(block: (UserPreferences) -> UserPreferences) {
        // Handled via individual updates
    }

    suspend fun setArabicFontSize(size: Float) {
        dataStore.edit { it[PrefKeys.ARABIC_FONT_SIZE] = size }
    }

    suspend fun setTranslationFontSize(size: Float) {
        dataStore.edit { it[PrefKeys.TRANSLATION_FONT_SIZE] = size }
    }

    suspend fun toggleTransliteration(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.SHOW_TRANSLITERATION] = enabled }
    }

    suspend fun toggleUrdu(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.SHOW_URDU] = enabled }
    }

    suspend fun toggleEnglish(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.SHOW_ENGLISH] = enabled }
    }

    suspend fun toggleWordByWord(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.SHOW_WORD_BY_WORD] = enabled }
    }

    suspend fun setReciter(reciter: Reciter) {
        dataStore.edit { it[PrefKeys.RECITER_ID] = reciter.id }
    }

    suspend fun setPlaybackSpeed(speed: Float) {
        dataStore.edit { it[PrefKeys.PLAYBACK_SPEED] = speed }
    }

    suspend fun setDailyReadingGoal(goal: Int) {
        dataStore.edit { it[PrefKeys.DAILY_READING_GOAL] = goal }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.DARK_THEME] = enabled }
    }

    suspend fun setEyeComfortMode(enabled: Boolean) {
        dataStore.edit { it[PrefKeys.EYE_COMFORT_MODE] = enabled }
    }

    suspend fun setAppLanguage(lang: String) {
        dataStore.edit { it[PrefKeys.APP_LANGUAGE] = lang }
    }

    // Bookmarks
    val bookmarksFlow: Flow<List<BookmarkEntity>> = bookmarkDao.getAllBookmarks()

    suspend fun isBookmarked(ayahNumber: Int): Boolean {
        return bookmarkDao.getBookmarkByAyah(ayahNumber) != null
    }

    suspend fun toggleBookmark(ayahNumber: Int, note: String? = null) {
        val existing = bookmarkDao.getBookmarkByAyah(ayahNumber)
        if (existing != null) {
            bookmarkDao.deleteByAyah(ayahNumber)
        } else {
            bookmarkDao.insertBookmark(BookmarkEntity(ayahNumber = ayahNumber, note = note))
        }
    }

    // Hifz Progress
    val hifzProgressFlow: Flow<List<HifzProgressEntity>> = hifzDao.getAllProgress()

    suspend fun setHifzStatus(ayahNumber: Int, status: String) {
        hifzDao.insertOrUpdateProgress(
            HifzProgressEntity(
                ayahNumber = ayahNumber,
                status = status,
                reviewCount = 1,
                lastTestedTimestamp = System.currentTimeMillis()
            )
        )
    }

    // Quiz records
    val recentQuizzesFlow: Flow<List<QuizRecordEntity>> = quizDao.getRecentQuizzes()

    suspend fun saveQuizRecord(quizType: String, score: Int, total: Int) {
        quizDao.insertQuizRecord(
            QuizRecordEntity(
                quizType = quizType,
                score = score,
                totalQuestions = total
            )
        )
    }

    // Journal
    val journalFlow: Flow<List<JournalEntity>> = journalDao.getAllReflections()

    suspend fun saveReflection(ayahNumber: Int, text: String) {
        journalDao.insertReflection(
            JournalEntity(
                ayahNumber = ayahNumber,
                reflectionText = text
            )
        )
    }

    suspend fun deleteReflection(journal: JournalEntity) {
        journalDao.deleteReflection(journal)
    }
}
