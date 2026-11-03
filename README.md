# Surah Yaseen (سُورَة يس) — Complete Quran Learning & AI Companion

A native, spiritually calming Android application crafted with Kotlin and Jetpack Compose (Material 3), dedicated exclusively to **Surah Yaseen (Surah 36 of the Holy Quran, containing all 83 ayahs)**.

---

## Key Features

1. **Complete 83-Ayah Quran Reader**
   - Verified canonical Uthmani Arabic text with diacritical marks.
   - Dual translations: English (Sahih International) and Urdu (Fateh Muhammad Jalandhri).
   - Clear phonetic transliteration for English readers.
   - Word-by-word interactive mode with roots (جذر), part of speech (اسم, فعل, حرف), and grammatical roles (إعراب).
   - Custom slider controls for Arabic font sizing (22sp–42sp) and translation sizing (13sp–24sp).
   - Fast keyword, ayah number, and Arabic root search.
   - Jump directly to any ayah with one tap.

2. **Audio Recitation & Playback**
   - High-fidelity CDN audio streaming for Mishary Rashid Alafasy, Abdul Basit Abdul Samad, and Saad Al-Ghamdi.
   - Ayah repetition (1x, 3x, 5x, 10x) and repeat ranges for memorization.
   - Adjustable playback speeds (0.75x, 1.0x, 1.25x, 1.5x).
   - Built-in sleep timer (15, 30, 45, 60 minutes) for bedtime listening.
   - Real-time active ayah highlighting and sticky mini player bar across screens.

3. **Tajweed Mastery**
   - Color-coded interactive Tajweed guide covering Ghunnah, Idgham (with and without Ghunnah), Ikhfa, Iqlab, Izhar, Qalqalah, and Madd.
   - Exact examples extracted directly from Surah Yaseen with beginner-friendly pronunciation tips.

4. **Quranic Arabic Grammar (Nahw & Sarf)**
   - Lessons on the three universal word classes (Ism, Fi'l, Harf).
   - The Quranic root system and verb patterns (Awzan).
   - The three I'rab cases: Marfoo' (مرفوع), Mansoob (منصوب), and Majroor (مجرور).
   - Emphatic Quranic sentence constructions (Inna & Her sisters).
   - Interactive Root Word Explorer browsing all roots occurring across Surah Yaseen.

5. **Hifz & Memorization Studio**
   - 83-ayah visual progress tracker (Not Started, Learning, Memorized, Mastered).
   - Progressive concealment modes: Full Text, Tap-to-Reveal word-by-word, and Blank Recall.
   - Self-assessment ratings and review milestones.

6. **Recitation Recording & Self-Practice**
   - Microphone recording with runtime permission.
   - Instant audio comparison with the reference recitation.
   - Local on-device storage ensuring complete privacy.

7. **Grounded Gemini AI Companion**
   - Answers questions in English, Urdu (اردو), and Roman Urdu.
   - Grounded context injection linking responses to exact ayahs in Surah Yaseen.
   - Suggested prompts for beginners and advanced students.
   - Theological disclaimer establishing educational companion boundaries.

8. **Authentic Hadith References**
   - Researched narrations with collection references (Jami` at-Tirmidhi, Abu Dawud, Darimi).
   - Scholarly grading and assessments (Sahih, Da'eef, Jayyid) by renowned Hadith verifiers.

9. **Daily Reflection & Personal Journal**
   - Daily ayah spotlight with reflection prompts.
   - Local Room SQLite persistence for bookmarks and personal notes.

---

## Technical Architecture

- **Language**: Kotlin 2.2.10
- **UI Toolkit**: Jetpack Compose with Material 3 ("Sacred Garden" emerald and gold aesthetic)
- **Local Persistence**: Android Room Database (Bookmarks, Hifz milestones, Quiz scores, Reflections) + DataStore Preferences
- **Audio Engine**: Android native `MediaPlayer` with lifecycle-safe state flows
- **AI Integration**: Gemini 3.5 Flash via direct authenticated REST client with 60-second timeouts and RAG prompt grounding
- **Offline Support**: Canonical 83 ayahs, translations, Tajweed rules, and grammar data are pre-bundled in `app/src/main/assets/`

---

## How to Test & Build

### Running JVM Unit Tests
```bash
gradle :app:testDebugUnitTest
```

### Building the Debug APK
```bash
gradle :app:assembleDebug
```
The resulting APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Configuring the Gemini API Key
To enable the AI Learning Tutor:
1. Open the **Secrets panel in Google AI Studio**.
2. Add your `GEMINI_API_KEY`.
3. The Secrets Gradle Plugin injects the key securely into `BuildConfig.GEMINI_API_KEY` at build time.
