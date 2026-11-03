package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ChatMessage
import com.example.data.repository.GeminiRepository
import com.example.data.repository.MessageSender
import com.example.data.repository.QuranRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val selectedContextAyah: Int? = null,
    val errorMessage: String? = null,
    val suggestedQuestions: List<String> = listOf(
        "What are the central themes of Surah An-Naba (The Great News)?",
        "سورہ النبأ کی ابتدائی آیات کا خلاصہ آسان اردو میں بیان کریں",
        "What is the root, Nahw and Sarf analysis of 'عَمَّ يَتَسَآءَلُونَ'?",
        "Explain why the mountains are described as 'أَوْتَادًا' (pegs) in Ayah 7",
        "Explain the descriptions of Jannat an-Na'eem (Gardens of Delight) in Ayahs 31–36",
        "What is the significance of 'يَوْمَ يَقُومُ ٱلرُّوحُ وَٱلْمَلَٰٓئِكَةُ صَفًّا' in Ayah 38?"
    )
)

class AiChatViewModel(
    private val geminiRepository: GeminiRepository,
    private val quranRepository: QuranRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    init {
        // Welcome message
        val welcomeMsg = ChatMessage(
            sender = MessageSender.AI,
            text = "As-salamu alaykum! I am your AI Quran & Arabic Grammar Companion for Surah An-Naba (Juz 'Amma, Para 30). Ask me about verse meanings, word roots (جذور), Arabic grammar (Nahw & Sarf), Tajweed rules, or classical tafsir in English, Urdu (اردو), or Roman Urdu."
        )
        _uiState.value = _uiState.value.copy(messages = listOf(welcomeMsg))
    }

    fun setContextAyah(ayahNumber: Int?) {
        _uiState.value = _uiState.value.copy(selectedContextAyah = ayahNumber)
    }

    fun sendMessage(query: String) {
        if (query.isBlank() || _uiState.value.isGenerating) return

        val userMsg = ChatMessage(
            sender = MessageSender.USER,
            text = query,
            referencedAyah = _uiState.value.selectedContextAyah
        )

        val updatedMessages = _uiState.value.messages + userMsg
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            isGenerating = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val result = geminiRepository.askAssistant(
                userQuery = query,
                contextAyahNumber = _uiState.value.selectedContextAyah,
                chatHistory = updatedMessages
            )

            result.fold(
                onSuccess = { answer ->
                    val aiMsg = ChatMessage(
                        sender = MessageSender.AI,
                        text = answer,
                        referencedAyah = _uiState.value.selectedContextAyah
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + aiMsg,
                        isGenerating = false
                    )
                },
                onFailure = { error ->
                    val errorMsg = ChatMessage(
                        sender = MessageSender.SYSTEM,
                        text = "Notice: ${error.localizedMessage ?: "Unable to connect to AI assistant. Please check your internet connection or API key."}"
                    )
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + errorMsg,
                        isGenerating = false,
                        errorMessage = error.localizedMessage
                    )
                }
            )
        }
    }

    fun clearChat() {
        val welcomeMsg = ChatMessage(
            sender = MessageSender.AI,
            text = "Conversation cleared. How can I help you study Surah An-Naba today?"
        )
        _uiState.value = _uiState.value.copy(
            messages = listOf(welcomeMsg),
            errorMessage = null
        )
    }
}
