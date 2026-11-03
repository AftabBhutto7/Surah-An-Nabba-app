package com.example.data.repository

import android.content.Context
import com.example.data.model.Ayah
import com.example.data.model.GrammarExample
import com.example.data.model.GrammarLesson
import com.example.data.model.HadithNarration
import com.example.data.model.SurahData
import com.example.data.model.TajweedRule
import com.example.data.model.TajweedToken
import com.example.data.model.Word
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader

class QuranRepository(private val context: Context) {

    private var cachedSurahData: SurahData? = null
    private var cachedTajweedRules: List<TajweedRule>? = null
    private var cachedGrammarLessons: List<GrammarLesson>? = null
    private var cachedHadiths: List<HadithNarration>? = null

    @Synchronized
    fun getSurahData(): SurahData {
        cachedSurahData?.let { return it }

        val jsonString = context.assets.open("surah_naba.json").use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).readText()
        }
        val rootObj = JSONObject(jsonString)

        val surahNameAr = rootObj.optString("surahNameArabic", "النبأ")
        val surahNameEn = rootObj.optString("surahNameEnglish", "An-Naba")
        val surahNameMeaning = rootObj.optString("surahNameMeaning", "The Great News (Juz 'Amma)")
        val totalAyahs = rootObj.optInt("totalAyahs", 40)
        val revelationType = rootObj.optString("revelationType", "Meccan")
        val rukuCount = rootObj.optInt("rukuCount", 2)

        val ayahsArray = rootObj.optJSONArray("ayahs") ?: JSONArray()
        val ayahsList = mutableListOf<Ayah>()

        for (i in 0 until ayahsArray.length()) {
            val aObj = ayahsArray.getJSONObject(i)
            val num = aObj.optInt("number", i + 1)
            val arabic = aObj.optString("arabicUthmani", "")
            val en = aObj.optString("translationEn", "")
            val ur = aObj.optString("translationUr", "")
            val tr = aObj.optString("transliteration", "")
            val theme = aObj.optString("theme", "Surah An-Naba")
            val audioAlafasy = aObj.optString("audioUrlAlafasy", "")
            val audioBasit = aObj.optString("audioUrlAbdulBasit", "")
            val audioGhamadi = aObj.optString("audioUrlGhamadi", "")

            // Parse words
            val wordsArray = aObj.optJSONArray("words") ?: JSONArray()
            val wordsList = mutableListOf<Word>()
            for (w in 0 until wordsArray.length()) {
                val wObj = wordsArray.getJSONObject(w)
                wordsList.add(
                    Word(
                        position = wObj.optInt("position", w + 1),
                        arabic = wObj.optString("arabic", ""),
                        translationEn = wObj.optString("translationEn", ""),
                        translationUr = wObj.optString("translationUr", ""),
                        transliteration = wObj.optString("transliteration", ""),
                        root = wObj.optString("root", "—"),
                        partOfSpeech = wObj.optString("partOfSpeech", "كلمة"),
                        grammarRole = wObj.optString("grammarRole", ""),
                        caseState = wObj.optString("caseState", ""),
                        explanation = wObj.optString("explanation", "")
                    )
                )
            }

            // Parse tajweed tokens
            val tjArray = aObj.optJSONArray("tajweedTokens") ?: JSONArray()
            val tjList = mutableListOf<TajweedToken>()
            for (t in 0 until tjArray.length()) {
                val tObj = tjArray.getJSONObject(t)
                tjList.add(
                    TajweedToken(
                        rule = tObj.optString("rule", ""),
                        arabic = tObj.optString("arabic", ""),
                        explanation = tObj.optString("explanation", "")
                    )
                )
            }

            ayahsList.add(
                Ayah(
                    number = num,
                    surahNumber = 78,
                    arabicUthmani = arabic,
                    translationEn = en,
                    translationUr = ur,
                    transliteration = tr,
                    theme = theme,
                    audioUrlAlafasy = audioAlafasy,
                    audioUrlAbdulBasit = audioBasit,
                    audioUrlGhamadi = audioGhamadi,
                    words = wordsList,
                    tajweedTokens = tjList
                )
            )
        }

        val data = SurahData(
            surahNameArabic = surahNameAr,
            surahNameEnglish = surahNameEn,
            surahNameMeaning = surahNameMeaning,
            totalAyahs = totalAyahs,
            revelationType = revelationType,
            rukuCount = rukuCount,
            ayahs = ayahsList
        )
        cachedSurahData = data
        return data
    }

    fun getAllAyahs(): List<Ayah> {
        return getSurahData().ayahs
    }

    fun getAyah(number: Int): Ayah? {
        val ayahs = getAllAyahs()
        return ayahs.getOrNull(number - 1)
    }

    fun search(query: String): List<Ayah> {
        if (query.isBlank()) return getAllAyahs()
        val q = query.trim().lowercase()
        return getAllAyahs().filter { ayah ->
            ayah.number.toString() == q ||
                    ayah.arabicUthmani.contains(query, ignoreCase = true) ||
                    ayah.translationEn.lowercase().contains(q) ||
                    ayah.translationUr.contains(query, ignoreCase = true) ||
                    ayah.transliteration.lowercase().contains(q) ||
                    ayah.words.any { it.root.contains(query, ignoreCase = true) || it.translationEn.lowercase().contains(q) }
        }
    }

    @Synchronized
    fun getTajweedRules(): List<TajweedRule> {
        cachedTajweedRules?.let { return it }
        val jsonString = context.assets.open("tajweed_rules.json").use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).readText()
        }
        val array = JSONArray(jsonString)
        val list = mutableListOf<TajweedRule>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val examplesArray = obj.optJSONArray("examples") ?: JSONArray()
            val examplesList = mutableListOf<String>()
            for (e in 0 until examplesArray.length()) {
                examplesList.add(examplesArray.getString(e))
            }
            list.add(
                TajweedRule(
                    id = obj.optString("id", ""),
                    nameArabic = obj.optString("nameArabic", ""),
                    nameEnglish = obj.optString("nameEnglish", ""),
                    colorHex = obj.optString("colorHex", "#1B4332"),
                    description = obj.optString("description", ""),
                    examples = examplesList,
                    practiceTips = obj.optString("practiceTips", "")
                )
            )
        }
        cachedTajweedRules = list
        return list
    }

    @Synchronized
    fun getGrammarLessons(): List<GrammarLesson> {
        cachedGrammarLessons?.let { return it }
        val jsonString = context.assets.open("grammar_naba.json").use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).readText()
        }
        val array = JSONArray(jsonString)
        val list = mutableListOf<GrammarLesson>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val exArray = obj.optJSONArray("surahExamples") ?: JSONArray()
            val exList = mutableListOf<GrammarExample>()
            for (e in 0 until exArray.length()) {
                val exObj = exArray.getJSONObject(e)
                exList.add(
                    GrammarExample(
                        arabic = exObj.optString("arabic", ""),
                        breakdown = exObj.optString("breakdown", "")
                    )
                )
            }
            list.add(
                GrammarLesson(
                    id = obj.optInt("id", i + 1),
                    titleEn = obj.optString("titleEn", ""),
                    titleUr = obj.optString("titleUr", ""),
                    level = obj.optString("level", "Beginner"),
                    description = obj.optString("description", ""),
                    content = obj.optString("content", ""),
                    surahExamples = exList
                )
            )
        }
        cachedGrammarLessons = list
        return list
    }

    @Synchronized
    fun getHadithNarrations(): List<HadithNarration> {
        cachedHadiths?.let { return it }
        val jsonString = context.assets.open("hadith_naba.json").use { inputStream ->
            InputStreamReader(inputStream, Charsets.UTF_8).readText()
        }
        val array = JSONArray(jsonString)
        val list = mutableListOf<HadithNarration>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                HadithNarration(
                    id = obj.optInt("id", i + 1),
                    titleEn = obj.optString("titleEn", ""),
                    titleUr = obj.optString("titleUr", ""),
                    arabicText = obj.optString("arabicText", ""),
                    translationEn = obj.optString("translationEn", ""),
                    translationUr = obj.optString("translationUr", ""),
                    sourceCollection = obj.optString("sourceCollection", ""),
                    referenceId = obj.optString("referenceId", ""),
                    scholarlyGrading = obj.optString("scholarlyGrading", ""),
                    scholarlyCommentary = obj.optString("scholarlyCommentary", "")
                )
            )
        }
        cachedHadiths = list
        return list
    }

    fun getAllRoots(): List<String> {
        val set = mutableSetOf<String>()
        getAllAyahs().forEach { ayah ->
            ayah.words.forEach { word ->
                if (word.root != "—" && word.root.isNotBlank()) {
                    set.add(word.root)
                }
            }
        }
        return set.toList().sorted()
    }

    fun getWordsByRoot(root: String): List<Pair<Ayah, Word>> {
        val result = mutableListOf<Pair<Ayah, Word>>()
        getAllAyahs().forEach { ayah ->
            ayah.words.forEach { word ->
                if (word.root.equals(root, ignoreCase = true)) {
                    result.add(Pair(ayah, word))
                }
            }
        }
        return result
    }
}
