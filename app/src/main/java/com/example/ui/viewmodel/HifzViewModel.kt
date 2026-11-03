package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.HifzProgressEntity
import com.example.data.model.Ayah
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class ConcealmentMode {
    FULL_VISIBLE,
    TAP_TO_REVEAL,
    BLANK_RECALL
}

data class HifzUiState(
    val currentAyahNumber: Int = 1,
    val currentAyah: Ayah? = null,
    val concealmentMode: ConcealmentMode = ConcealmentMode.FULL_VISIBLE,
    val revealedWordPositions: Set<Int> = emptySet(),
    val isTranslationRevealed: Boolean = false,
    val hifzMap: Map<Int, HifzProgressEntity> = emptyMap(),
    val totalMemorized: Int = 0,
    val totalLearning: Int = 0,
    val totalMastered: Int = 0
)

class HifzViewModel(
    private val quranRepository: QuranRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HifzUiState())
    val uiState: StateFlow<HifzUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        val ayah1 = quranRepository.getAyah(1)
        _uiState.value = _uiState.value.copy(
            currentAyahNumber = 1,
            currentAyah = ayah1
        )

        viewModelScope.launch {
            userDataRepository.hifzProgressFlow.collect { progressList ->
                val map = progressList.associateBy { it.ayahNumber }
                val memorized = progressList.count { it.status == "MEMORIZED" }
                val learning = progressList.count { it.status == "LEARNING" }
                val mastered = progressList.count { it.status == "MASTERED" }

                _uiState.value = _uiState.value.copy(
                    hifzMap = map,
                    totalMemorized = memorized,
                    totalLearning = learning,
                    totalMastered = mastered
                )
            }
        }
    }

    fun selectAyah(ayahNumber: Int) {
        val maxAyah = quranRepository.getSurahData().totalAyahs
        val clamped = ayahNumber.coerceIn(1, maxAyah)
        val ayah = quranRepository.getAyah(clamped)
        _uiState.value = _uiState.value.copy(
            currentAyahNumber = clamped,
            currentAyah = ayah,
            revealedWordPositions = emptySet(),
            isTranslationRevealed = false
        )
    }

    fun nextAyah() {
        selectAyah(_uiState.value.currentAyahNumber + 1)
    }

    fun previousAyah() {
        selectAyah(_uiState.value.currentAyahNumber - 1)
    }

    fun setConcealmentMode(mode: ConcealmentMode) {
        _uiState.value = _uiState.value.copy(
            concealmentMode = mode,
            revealedWordPositions = if (mode == ConcealmentMode.FULL_VISIBLE) {
                _uiState.value.currentAyah?.words?.map { it.position }?.toSet() ?: emptySet()
            } else emptySet()
        )
    }

    fun toggleRevealWord(position: Int) {
        val currentSet = _uiState.value.revealedWordPositions.toMutableSet()
        if (currentSet.contains(position)) {
            currentSet.remove(position)
        } else {
            currentSet.add(position)
        }
        _uiState.value = _uiState.value.copy(revealedWordPositions = currentSet)
    }

    fun revealAllWords() {
        val allPositions = _uiState.value.currentAyah?.words?.map { it.position }?.toSet() ?: emptySet()
        _uiState.value = _uiState.value.copy(revealedWordPositions = allPositions)
    }

    fun toggleTranslation() {
        _uiState.value = _uiState.value.copy(isTranslationRevealed = !_uiState.value.isTranslationRevealed)
    }

    fun markStatus(status: String) {
        val ayahNum = _uiState.value.currentAyahNumber
        viewModelScope.launch {
            userDataRepository.setHifzStatus(ayahNum, status)
        }
    }
}
