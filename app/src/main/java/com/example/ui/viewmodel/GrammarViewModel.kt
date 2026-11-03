package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.data.model.Ayah
import com.example.data.model.GrammarLesson
import com.example.data.model.HadithNarration
import com.example.data.model.TajweedRule
import com.example.data.model.Word
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LearnTab {
    TAJWEED, GRAMMAR, ROOTS, HADITH
}

data class GrammarUiState(
    val currentTab: LearnTab = LearnTab.TAJWEED,
    val tajweedRules: List<TajweedRule> = emptyList(),
    val selectedTajweedRule: TajweedRule? = null,
    val grammarLessons: List<GrammarLesson> = emptyList(),
    val selectedLesson: GrammarLesson? = null,
    val allRoots: List<String> = emptyList(),
    val selectedRoot: String? = null,
    val rootOccurrences: List<Pair<Ayah, Word>> = emptyList(),
    val rootSearchQuery: String = "",
    val hadithNarrations: List<HadithNarration> = emptyList()
)

class GrammarViewModel(private val quranRepository: QuranRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(GrammarUiState())
    val uiState: StateFlow<GrammarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val tajweed = quranRepository.getTajweedRules()
        val lessons = quranRepository.getGrammarLessons()
        val roots = quranRepository.getAllRoots()
        val hadiths = quranRepository.getHadithNarrations()

        val firstRoot = roots.firstOrNull() ?: "ق-و-ل"
        val firstOccurrences = quranRepository.getWordsByRoot(firstRoot)

        _uiState.value = _uiState.value.copy(
            tajweedRules = tajweed,
            selectedTajweedRule = tajweed.firstOrNull(),
            grammarLessons = lessons,
            selectedLesson = lessons.firstOrNull(),
            allRoots = roots,
            selectedRoot = firstRoot,
            rootOccurrences = firstOccurrences,
            hadithNarrations = hadiths
        )
    }

    fun selectTab(tab: LearnTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun selectTajweedRule(rule: TajweedRule) {
        _uiState.value = _uiState.value.copy(selectedTajweedRule = rule)
    }

    fun selectLesson(lesson: GrammarLesson) {
        _uiState.value = _uiState.value.copy(selectedLesson = lesson)
    }

    fun selectRoot(root: String) {
        val occurrences = quranRepository.getWordsByRoot(root)
        _uiState.value = _uiState.value.copy(
            selectedRoot = root,
            rootOccurrences = occurrences
        )
    }

    fun onRootSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(rootSearchQuery = query)
    }
}
