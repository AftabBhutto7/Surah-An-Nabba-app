package com.example.data.model

data class SurahData(
    val surahNameArabic: String,
    val surahNameEnglish: String,
    val surahNameMeaning: String,
    val totalAyahs: Int,
    val revelationType: String,
    val rukuCount: Int,
    val ayahs: List<Ayah>
)

data class Ayah(
    val number: Int,
    val surahNumber: Int = 78,
    val arabicUthmani: String,
    val translationEn: String,
    val translationUr: String,
    val transliteration: String,
    val theme: String,
    val audioUrlAlafasy: String,
    val audioUrlAbdulBasit: String,
    val audioUrlGhamadi: String,
    val words: List<Word>,
    val tajweedTokens: List<TajweedToken>
)

data class Word(
    val position: Int,
    val arabic: String,
    val translationEn: String,
    val translationUr: String = "",
    val transliteration: String = "",
    val root: String = "—",
    val partOfSpeech: String = "كلمة",
    val grammarRole: String = "معربة بحسب السياق",
    val caseState: String = "معرب / مبني",
    val explanation: String = ""
)

data class TajweedToken(
    val rule: String,
    val arabic: String,
    val explanation: String
)

data class TajweedRule(
    val id: String,
    val nameArabic: String,
    val nameEnglish: String,
    val colorHex: String,
    val description: String,
    val examples: List<String>,
    val practiceTips: String
)

data class GrammarLesson(
    val id: Int,
    val titleEn: String,
    val titleUr: String,
    val level: String,
    val description: String,
    val content: String,
    val surahExamples: List<GrammarExample>
)

data class GrammarExample(
    val arabic: String,
    val breakdown: String
)

data class HadithNarration(
    val id: Int,
    val titleEn: String,
    val titleUr: String,
    val arabicText: String,
    val translationEn: String,
    val translationUr: String,
    val sourceCollection: String,
    val referenceId: String,
    val scholarlyGrading: String,
    val scholarlyCommentary: String
)

enum class Reciter(val id: String, val displayNameEn: String, val displayNameUr: String, val style: String) {
    ALAFASY("alafasy", "Mishary Rashid Alafasy", "مشاری راشد العفاسی", "Murattal"),
    ABDUL_BASIT("abdul_basit", "Abdul Basit Abdul Samad", "عبد الباسط عبد الصمد", "Murattal"),
    GHAMADI("ghamadi", "Saad Al-Ghamdi", "سعد الغامدی", "Murattal")
}

data class UserPreferences(
    val arabicFontSize: Float = 28f,
    val translationFontSize: Float = 16f,
    val showTransliteration: Boolean = true,
    val showUrduTranslation: Boolean = true,
    val showEnglishTranslation: Boolean = true,
    val showWordByWord: Boolean = false,
    val selectedReciter: Reciter = Reciter.ALAFASY,
    val playbackSpeed: Float = 1.0f,
    val dailyReadingGoalAyahs: Int = 10,
    val darkTheme: Boolean = false,
    val eyeComfortMode: Boolean = false,
    val appLanguage: String = "en" // "en" or "ur"
)
