package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.QuizRecordEntity
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuizQuestion(
    val id: Int,
    val type: String, // "VOCAB", "ROOT", "GRAMMAR", "TAJWEED", "NEXT_AYAH"
    val prompt: String,
    val arabicSnippet: String? = null,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

data class QuizUiState(
    val isQuizActive: Boolean = false,
    val isQuizFinished: Boolean = false,
    val questions: List<QuizQuestion> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val selectedAnswerIndex: Int? = null,
    val isAnswerSubmitted: Boolean = false,
    val currentScore: Int = 0,
    val recentQuizRecords: List<QuizRecordEntity> = emptyList()
)

class QuizViewModel(
    private val quranRepository: QuranRepository,
    private val userDataRepository: UserDataRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepository.recentQuizzesFlow.collect { records ->
                _uiState.value = _uiState.value.copy(recentQuizRecords = records)
            }
        }
    }

    fun startNewQuiz() {
        val generatedQuestions = generateSurahNabaQuestions()
        _uiState.value = _uiState.value.copy(
            isQuizActive = true,
            isQuizFinished = false,
            questions = generatedQuestions,
            currentQuestionIndex = 0,
            selectedAnswerIndex = null,
            isAnswerSubmitted = false,
            currentScore = 0
        )
    }

    private fun generateSurahNabaQuestions(): List<QuizQuestion> {
        return listOf(
            QuizQuestion(
                id = 1,
                type = "ROOT",
                prompt = "What are the three root letters (جذر) of 'يَتَسَآءَلُونَ' in Ayah 1?",
                arabicSnippet = "عَمَّ يَتَسَآءَلُونَ",
                options = listOf("س - ء - ل", "س - ل - م", "ت - س - ل", "و - س - ل"),
                correctIndex = 0,
                explanation = "The root is (س-ء-ل), meaning asking, inquiring, or questioning from Form VI (تَفَاعَلَ)."
            ),
            QuizQuestion(
                id = 2,
                type = "VOCAB",
                prompt = "What is the meaning of the word 'سُبَاتًا' in Ayah 9 ('وَجَعَلْنَا نَوْمَكُمْ سُبَاتًا')?",
                arabicSnippet = "وَجَعَلْنَا نَوْمَكُمْ سُبَاتًا",
                options = listOf("A rest / cessation of activity", "A heavy punishment", "A dark shadow", "A deep dream"),
                correctIndex = 0,
                explanation = "Subatan (سباتاً) means a cut-off from motion and activity, allowing the body and spirit complete rest."
            ),
            QuizQuestion(
                id = 3,
                type = "GRAMMAR",
                prompt = "In Ayah 7: 'وَٱلْجِبَالَ أَوْتَادًا', what is the meaning of 'أَوْتَادًا'?",
                arabicSnippet = "وَٱلْجِبَالَ أَوْتَادًا",
                options = listOf("Pegs / stakes anchoring the earth", "Golden pillars", "High towers", "Floating clouds"),
                correctIndex = 0,
                explanation = "Awtad (أوتاد) is the plural of Watad (peg/stake), referring to mountains anchoring the earth's lithosphere."
            ),
            QuizQuestion(
                id = 4,
                type = "TAJWEED",
                prompt = "Which Tajweed rule applies to 'سِرَاجًا وَهَّاجًا' in Ayah 13?",
                arabicSnippet = "وَجَعَلْنَا سِرَاجًا وَهَّاجًا",
                options = listOf("Idgham with Ghunnah (إدغام بغنة)", "Izhar Halqi (إظهار)", "Iqlab (إقلاب)", "Qalqalah Sughra (قلقلة)"),
                correctIndex = 0,
                explanation = "Tanween on 'سِرَاجًا' followed by the letter Waw (و) results in Idgham with a 2-count nasal Ghunnah."
            ),
            QuizQuestion(
                id = 5,
                type = "NEXT_AYAH",
                prompt = "Which verse immediately follows Ayah 1: 'عَمَّ يَتَسَآءَلُونَ'?",
                arabicSnippet = "عَمَّ يَتَسَآءَلُونَ",
                options = listOf("عَنِ ٱلنَّبَإِ ٱلْعَظِيمِ", "ٱلَّذِى هُمْ فِيهِ مُخْتَلِفُونَ", "كَلَّا سَيَعْلَمُونَ", "أَلَمْ نَجْعَلِ ٱلْأَرْضَ مِهَٰدًا"),
                correctIndex = 0,
                explanation = "Ayah 2 is: 'About the great news' (عَنِ ٱلنَّبَإِ ٱلْعَظِيمِ)."
            )
        )
    }

    fun selectAnswer(index: Int) {
        if (_uiState.value.isAnswerSubmitted) return
        _uiState.value = _uiState.value.copy(selectedAnswerIndex = index)
    }

    fun submitAnswer() {
        val state = _uiState.value
        val selected = state.selectedAnswerIndex ?: return
        val currentQ = state.questions.getOrNull(state.currentQuestionIndex) ?: return

        val isCorrect = selected == currentQ.correctIndex
        val newScore = if (isCorrect) state.currentScore + 1 else state.currentScore

        _uiState.value = state.copy(
            isAnswerSubmitted = true,
            currentScore = newScore
        )
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex < state.questions.size) {
            _uiState.value = state.copy(
                currentQuestionIndex = nextIndex,
                selectedAnswerIndex = null,
                isAnswerSubmitted = false
            )
        } else {
            // Quiz completed
            _uiState.value = state.copy(isQuizFinished = true)
            viewModelScope.launch {
                userDataRepository.saveQuizRecord(
                    quizType = "COMPREHENSIVE",
                    score = state.currentScore,
                    total = state.questions.size
                )
            }
        }
    }
}
