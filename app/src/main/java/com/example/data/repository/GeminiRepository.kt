package com.example.data.repository

import com.example.BuildConfig
import com.example.data.model.Ayah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val referencedAyah: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI, SYSTEM
}

class GeminiRepository(private val quranRepository: QuranRepository) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val systemPrompt = """
You are a knowledgeable, respectful, and source-grounded Quran Learning & Arabic Grammar Tutor specializing in Surah An-Naba (Surah 78) and the 30th Para (Juz 'Amma).
You understand English, Urdu, Roman Urdu, and Arabic.

Core Responsibilities:
1. Explain the meanings, themes, and classical interpretations (Tafsir Ibn Kathir, Tafsir al-Tabari, Ma'ariful Quran) of Surah An-Naba's 40 ayahs and the grand surahs of Juz 30.
2. Teach Quranic Arabic grammar: Nahw (syntax), Sarf (morphology), root letters (جذور), verb forms (أوزان), and I'rab (grammatical roles).
3. Explain Tajweed rules (Ghunnah, Ikhfa, Idgham, Qalqalah, Madd, etc.) with precise examples.
4. When answering, cite the exact Surah and Ayah number (e.g., [Surah An-Naba 78:17]).
5. Never invent or hallucinate Quranic Arabic verses, Hadith numbers, or scholarly rulings. If uncertain, state clearly that evidence is insufficient.
6. Clearly distinguish canonical Quranic text from human commentary and translations.
7. Maintain an educational perspective: remind users you are a learning companion, not a Mufti issuing personal legal rulings (Fatwas).
8. If asked in Urdu or Roman Urdu, respond naturally in Urdu or Roman Urdu with polite, clear explanations.
""".trimIndent()

    suspend fun askAssistant(
        userQuery: String,
        contextAyahNumber: Int? = null,
        chatHistory: List<ChatMessage> = emptyList()
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(
                IllegalStateException("Gemini API key is not configured. Please add your GEMINI_API_KEY in the Secrets panel or .env file.")
            )
        }

        try {
            // Retrieve relevant context from Surah An-Naba
            val contextBuilder = StringBuilder()
            val totalAyahs = quranRepository.getSurahData().totalAyahs
            if (contextAyahNumber != null && contextAyahNumber in 1..totalAyahs) {
                val ayah = quranRepository.getAyah(contextAyahNumber)
                if (ayah != null) {
                    contextBuilder.append("\n[CURRENT FOCUSED AYAH - Surah An-Naba 78:${ayah.number}]\n")
                    contextBuilder.append("Arabic: ${ayah.arabicUthmani}\n")
                    contextBuilder.append("English: ${ayah.translationEn}\n")
                    contextBuilder.append("Urdu: ${ayah.translationUr}\n")
                    contextBuilder.append("Transliteration: ${ayah.transliteration}\n")
                    contextBuilder.append("Theme: ${ayah.theme}\n")
                    contextBuilder.append("Words & Roots: ")
                    ayah.words.forEach { w ->
                        contextBuilder.append("${w.arabic} (Root: ${w.root}, Role: ${w.grammarRole}); ")
                    }
                    contextBuilder.append("\n")
                }
            }

            val contentsArray = JSONArray()

            // System prompt as first turn or system instruction
            val systemObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", "$systemPrompt\n$contextBuilder")))
            }
            contentsArray.put(systemObj)

            val systemAck = JSONObject().apply {
                put("role", "model")
                put("parts", JSONArray().put(JSONObject().put("text", "Understood. I am ready to guide you through Surah An-Naba with authentic Quranic text, verified Arabic grammar, and classical commentaries.")))
            }
            contentsArray.put(systemAck)

            // Previous turns (last 6 turns for context)
            val recentHistory = chatHistory.takeLast(6)
            for (msg in recentHistory) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                val turnObj = JSONObject().apply {
                    put("role", role)
                    put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                }
                contentsArray.put(turnObj)
            }

            // Current user turn
            val currentUserObj = JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().put("text", userQuery)))
            }
            contentsArray.put(currentUserObj)

            val requestJson = JSONObject().apply {
                put("contents", contentsArray)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                    put("topP", 0.95)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            // Direct REST endpoint
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optJSONObject("error")?.optString("message") ?: "API Error: ${response.code}"
                } catch (e: Exception) {
                    "Request failed with HTTP status ${response.code}"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val respJson = JSONObject(responseBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return@withContext Result.failure(Exception("No answer returned from AI model."))
            }

            val firstCandidate = candidates.getJSONObject(0)
            val contentObj = firstCandidate.optJSONObject("content")
            val parts = contentObj?.optJSONArray("parts")
            val replyText = parts?.optJSONObject(0)?.optString("text") ?: "No response generated."

            Result.success(replyText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
