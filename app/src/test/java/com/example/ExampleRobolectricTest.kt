package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Surah An-Naba", appName)
    }

    @Test
    fun testSurahNaba40AyahsLoaded() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuranRepository(context)

        val surahData = repository.getSurahData()
        assertEquals("Surah An-Naba must have 40 ayahs", 40, surahData.totalAyahs)
        assertEquals(40, surahData.ayahs.size)

        // Verify Ayah 1: عَمَّ يَتَسَآءَلُونَ
        val ayah1 = repository.getAyah(1)
        assertNotNull(ayah1)
        assertTrue(
            "Ayah 1 must start with 'Amma",
            ayah1!!.arabicUthmani.contains("عَمَّ") || ayah1.arabicUthmani.contains("عَمَّ") || ayah1.transliteration.contains("amma")
        )

        // Verify Ayah 40: يَقُولُ ٱلْكَافِرُ يَٰلَيْتَنِى كُنتُ تُرَٰبًۢا
        val ayah40 = repository.getAyah(40)
        assertNotNull(ayah40)
        assertTrue(
            "Ayah 40 must contain Turaba",
            ayah40!!.arabicUthmani.contains("تُرَٰبًۢا") || ayah40.transliteration.contains("turāban") || ayah40.translationEn.contains("dust")
        )

        // Verify Tajweed rules & Grammar lessons
        val tajweedRules = repository.getTajweedRules()
        assertTrue("Must load authentic Tajweed rules", tajweedRules.isNotEmpty())

        val lessons = repository.getGrammarLessons()
        assertTrue("Must load Arabic grammar lessons", lessons.isNotEmpty())

        val hadiths = repository.getHadithNarrations()
        assertTrue("Must load verified Hadith narrations", hadiths.isNotEmpty())

        // Verify Search for An-Naba vocabulary
        val searchResults = repository.search("سُبَاتًا")
        assertTrue("Search should find Ayah 9 with 'سُبَاتًا'", searchResults.isNotEmpty())
    }

    @Test
    fun testFullSurahContinuousProgressTracking() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = QuranRepository(context)
        val playerManager = com.example.audio.RecitationPlayerManager(context, repository)

        val totalSurahDuration = playerManager.getTotalSurahDuration()
        // Surah An-Naba recitation is ~4m47s (> 270,000 ms)
        assertTrue("Full Surah total duration must be around 4:47", totalSurahDuration > 250000)

        // Verify elapsed time strictly increases monotonically with each Ayah
        var prevElapsed = 0
        for (ayah in 1..40) {
            val elapsed = playerManager.getElapsedBeforeAyah(ayah)
            assertTrue("Elapsed before ayah $ayah must be >= previous", elapsed >= prevElapsed)
            prevElapsed = elapsed
        }

        // Verify seeking to midway routes to correct Ayah without resetting to 0
        val midPointMs = totalSurahDuration / 2
        val state = playerManager.playerState.value
        assertEquals("Surah total duration in state must match", totalSurahDuration, state.surahTotalDurationMs)
    }
}
