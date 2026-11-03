package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BookmarkEntity
import com.example.data.local.entities.HifzProgressEntity
import com.example.data.local.entities.JournalEntity
import com.example.data.model.Ayah
import com.example.data.model.UserPreferences
import com.example.data.model.Word
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class HomeUiState(
    val lastReadAyahNumber: Int = 1,
    val lastReadAyah: Ayah? = null,
    val dailyGoalAyahs: Int = 10,
    val dailyProgressCount: Int = 3,
    val dailyAyah: Ayah? = null,
    val todayWord: Word? = null,
    val todayWordAyah: Ayah? = null,
    val memorizedCount: Int = 0,
    val learningCount: Int = 0,
    val totalBookmarks: Int = 0,
    val reflections: List<JournalEntity> = emptyList(),
    val preferences: UserPreferences = UserPreferences()
)

class HomeViewModel(
    private val quranRepository: QuranRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val surahData = quranRepository.getSurahData()
        val allAyahs = surahData.ayahs
        val total = surahData.totalAyahs.coerceAtLeast(1)

        val dayIndex = (System.currentTimeMillis() / (1000 * 60 * 60 * 24)).toInt()
        val dailyAyahIndex = (dayIndex % total) + 1
        val dailyAyah = quranRepository.getAyah(dailyAyahIndex) ?: allAyahs.firstOrNull()

        // Today's word
        val sampleWordAyah = quranRepository.getAyah(6) ?: allAyahs.firstOrNull()
        val todayWord = sampleWordAyah?.words?.firstOrNull { it.root != "—" } ?: sampleWordAyah?.words?.firstOrNull()

        _uiState.value = _uiState.value.copy(
            dailyAyah = dailyAyah,
            todayWord = todayWord,
            todayWordAyah = sampleWordAyah
        )

        viewModelScope.launch {
            combine(
                userDataRepository.lastReadAyahFlow,
                userDataRepository.userPreferencesFlow,
                userDataRepository.hifzProgressFlow,
                userDataRepository.bookmarksFlow,
                userDataRepository.journalFlow
            ) { lastRead, prefs, hifzList, bookmarks, journal ->
                val lastAyah = quranRepository.getAyah(lastRead)
                val memorized = hifzList.count { it.status == "MEMORIZED" || it.status == "MASTERED" }
                val learning = hifzList.count { it.status == "LEARNING" }

                _uiState.value.copy(
                    lastReadAyahNumber = lastRead,
                    lastReadAyah = lastAyah,
                    dailyGoalAyahs = prefs.dailyReadingGoalAyahs,
                    dailyProgressCount = (lastRead % prefs.dailyReadingGoalAyahs).coerceAtLeast(1),
                    memorizedCount = memorized,
                    learningCount = learning,
                    totalBookmarks = bookmarks.size,
                    reflections = journal,
                    preferences = prefs
                )
            }.collect { updatedState ->
                _uiState.value = updatedState
            }
        }
    }

    fun saveReflection(ayahNumber: Int, text: String) {
        viewModelScope.launch {
            userDataRepository.saveReflection(ayahNumber, text)
        }
    }
}
