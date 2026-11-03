package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.BookmarkEntity
import com.example.data.model.Ayah
import com.example.data.model.UserPreferences
import com.example.data.model.Word
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReaderUiState(
    val ayahs: List<Ayah> = emptyList(),
    val filteredAyahs: List<Ayah> = emptyList(),
    val searchQuery: String = "",
    val activeAyahNumber: Int = 1,
    val selectedWordForDetail: Word? = null,
    val selectedAyahForWord: Ayah? = null,
    val bookmarksMap: Map<Int, BookmarkEntity> = emptyMap(),
    val preferences: UserPreferences = UserPreferences()
)

class ReaderViewModel(
    private val quranRepository: QuranRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val allAyahs = quranRepository.getAllAyahs()
        _uiState.value = _uiState.value.copy(
            ayahs = allAyahs,
            filteredAyahs = allAyahs
        )

        viewModelScope.launch {
            combine(
                userDataRepository.userPreferencesFlow,
                userDataRepository.bookmarksFlow,
                userDataRepository.lastReadAyahFlow
            ) { prefs, bookmarks, lastRead ->
                Triple(prefs, bookmarks.associateBy { it.ayahNumber }, lastRead)
            }.collect { (prefs, bookMap, lastRead) ->
                _uiState.value = _uiState.value.copy(
                    preferences = prefs,
                    bookmarksMap = bookMap,
                    activeAyahNumber = lastRead
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val filtered = if (query.isBlank()) {
            _uiState.value.ayahs
        } else {
            quranRepository.search(query)
        }
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            filteredAyahs = filtered
        )
    }

    fun selectWordDetail(word: Word?, ayah: Ayah?) {
        _uiState.value = _uiState.value.copy(
            selectedWordForDetail = word,
            selectedAyahForWord = ayah
        )
    }

    fun updateActiveAyah(number: Int) {
        _uiState.value = _uiState.value.copy(activeAyahNumber = number)
        viewModelScope.launch {
            userDataRepository.setLastReadAyah(number)
        }
    }

    fun toggleBookmark(ayahNumber: Int, note: String? = null) {
        viewModelScope.launch {
            userDataRepository.toggleBookmark(ayahNumber, note)
        }
    }
}
