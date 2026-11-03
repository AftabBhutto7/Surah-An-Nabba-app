# Surah Yaseen — Complete Quran Learning & AI Companion

A comprehensive, spiritually calming native Android application dedicated to Surah Yaseen (Surah 36, 83 ayahs) combining an authentic Quran reader with verse-by-verse and word-by-word translations, audio recitation with sync, interactive Tajweed and Arabic grammar (Nahw & Sarf) lessons, Hifz memorization tools, and a source-grounded Gemini AI learning companion.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and UX choices have been confirmed and will guide the implementation:

- **Confirmed: Pre-bundled Canonical Dataset**: All 83 ayahs of Surah Yaseen—featuring canonical Uthmani Arabic text, English (Sahih International) and Urdu (Fateh Muhammad Jalandhri) translations, transliterations, word-by-word root analysis (جذر), grammatical tags (اسم, فعل, حرف), and verified Tajweed classifications—are bundled in assets for zero-latency, 100% reliable offline reading.
- **Confirmed: Recitation Streaming & Management**: Mishary Rashid Alafasy is the primary default reciter, with selectable options for Abdul Basit Abdul Samad and Saad Al-Ghamdi. Features complete play/pause, seek, ayah repetition, repeat range, speed control, sleep timer, and audio caching.
- **Confirmed: Balanced Dashboard Spotlight**: The Home dashboard highlights a balanced experience: Last Read & Reading Progress, Audio Quick-Play, Today's Grammar & Vocabulary spotlight, Hifz Memorization status, Daily Reflection from Surah Yaseen, and one-tap AI Assistant guidance.
- **AI Safety & Theological Grounding**: The Gemini assistant is grounded via strict context-retrieval (RAG) prompts against canonical Surah Yaseen text and authentic commentaries. It includes clear Islamic scholarship disclaimers and supports Urdu, English, and Roman Urdu.

---

## 1. Overview & Core Concept

- **What It Does**: Provides an all-in-one devotional and educational platform for Surah Yaseen: reading with customizable Arabic typography, listening to world-renowned reciters, learning word-by-word morphology and sentence syntax (I'rab), mastering Tajweed rules, memorizing verses through progressive concealment, and asking questions to an AI Quran tutor.
- **Target Audience / Persona**: Quran students, daily reciters, learners of Quranic Arabic, Hifz students memorizing the "Heart of the Quran", and general readers seeking verified translations and reflections in English and Urdu.
- **Key Value**: Bridges the gap between passive recitation and deep comprehension by coupling authentic Quranic text with interactive linguistic tools (root search, Nahw/Sarf breakdowns, Tajweed interactive legend) and source-grounded AI explanation.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Dashboard & Quick Navigation**: The user opens the app to an emerald-and-gold home dashboard displaying their last read ayah (e.g., Ayah 12), daily goal progress, "Today's Word" (e.g., *مُحْضَرُونَ* with root and morphology), audio player quick-bar, and direct shortcuts to reading, memorization, and AI chat.
2. **Immersive Quran Reader**: The user reads all 83 ayahs in high-legibility Arabic script (with Uthmani / Indo-Pak font size adjustment and line height tuning). Users can toggle English/Urdu translations, view word-by-word breakdowns, bookmark ayahs, add personal study notes, or jump to any ayah.
3. **Audio Recitation & Playback**: Users tap "Play Surah Yaseen" to launch playback from Ayah 1 to 83. The active ayah is highlighted in real time. Users can repeat an individual ayah (e.g., 3x or 5x for memorization), set a loop range (e.g., Ayahs 1–12), adjust playback speed (0.75x–1.5x), or set a 15/30/60-min sleep timer.
4. **Interactive Tajweed & Arabic Grammar**: Tapping on a word reveals its root letters (e.g., *س-ل-ك*), grammatical part of speech (Noun, Verb, Particle), case/state (Marfoo', Mansoob, Majroor), verb pattern/form, and beginner-friendly explanation. A Tajweed view highlights Ghunnah, Idgham, Ikhfa, Iqlab, Qalqalah, and Madd with color-coded interactive tokens.
5. **Hifz & Progressive Concealment**: Users practice memorization with progressive concealment modes (show full verse, hide words with tap-to-reveal, or blank test), ayah-order flashcards, and self-assessment scoring.
6. **AI Quran Assistant**: A dedicated chat screen where users can ask questions in English, Urdu, or Roman Urdu (e.g., *"What is the core message of Ayah 36–40?"*, *"Explain the root of فَمَالِكُونَ"*). The AI responds with verified ayah citations, linguistic context, and clear educational disclaimers.

### Visual Identity & Theme

- **Aesthetic Direction**: "Sacred Garden" — a tranquil, spiritually uplifting Islamic design aesthetic characterized by deep emerald greens, warm gold accents, soft cream surfaces in light mode, and deep midnight-slate backgrounds in dark mode.
- **Color Palette**:
  - `Primary`: Deep Forest Emerald (`#1B4332` Light / `#2D6A4F` Dark)
  - `OnPrimary`: Pure White (`#FFFFFF`)
  - `PrimaryContainer`: Soft Mint Tint (`#D8F3DC` Light / `#1E3F32` Dark)
  - `Accent / Gold`: Rich Artisan Gold (`#C8963E` / `#D4AF37`)
  - `Background`: Warm Pearl Ivory (`#FBFBF7` Light) / Deep Midnight Forest (`#0D1B17` Dark)
  - `Surface`: Pure White Card (`#FFFFFF` Light) / Elevated Dark Surface (`#172621` Dark)
  - `Tajweed Colors`: Ghunnah (Orange `#E65100`), Idgham (Sky Blue `#0288D1`), Ikhfa (Purple `#7B1FA2`), Qalqalah (Emerald Green `#2E7D32`), Madd (Crimson `#C2185B`).
- **Typography**:
  - Quranic Arabic: Traditional high-fidelity Arabic font family (`Amiri` / Naskh-style Quranic typeface) with full diacritical support, ligatures, and responsive RTL layout.
  - UI Display & Headers: Playfair Display / Cormorant Garamond aesthetic for titles, paired with clean, accessible Inter / Roboto for English and Urdu text.
- **Component Styling & Layout**:
  - Curved cards (16dp–24dp corners), subtle borders (`0.5.dp` gold or emerald outline), gold ayah-number rosettes (`۝`), and bottom navigation bar with active emerald pill indicators.

---

## 3. Key Product Decisions & Trade-Offs

- **Pre-bundled JSON Database vs. Network-Only Fetching**:
  - *Chosen Approach*: Bundled canonical 83-ayah dataset inside `assets/surah_yaseen.json` containing Arabic Uthmani text, transliteration, English (Sahih International), Urdu (Jalandhri), word-by-word roots, and Tajweed tags.
  - *Why*: Guarantees instant launch, zero reliance on external network stability for primary worship/reading, and eliminates any risk of malformed API payloads.
- **Audio Recitation Engine**:
  - *Chosen Approach*: Android `MediaPlayer` integration with high-speed CDN streaming for top reciters (Mishary Rashid Alafasy, Abdul Basit Abdul Samad), local caching support, ayah-level synchronization, and sleep timer.
  - *Why*: Native Android MediaPlayer provides low-latency, zero dependency conflicts, smooth lifecycle management, and reliable background playback.
- **AI Integration via Gemini 3.5 Flash / Gemini 3.8 Flash**:
  - *Chosen Approach*: Direct Retrofit REST client using `gemini-3.5-flash` with grounded system instructions, RAG-style ayah retrieval injection, and safety boundaries.
  - *Why*: High-speed, natural multilingual comprehension in Urdu, English, and Roman Urdu with strict instructions preventing unauthorized theological rulings or hallucinated verses.
- **Local Persistence with Room & DataStore**:
  - *Chosen Approach*: Room Database for bookmarks, personal reflection notes, Hifz test history, and quiz scores; DataStore for font size, reciter choice, audio speed, and theme preferences.
  - *Why*: Reliable, offline-first user data that persists across app restarts without requiring forced account sign-in.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
├────────────────────────────────────────────────────────────────────────┤
│  TopAppBar (Settings, Bookmarks, Audio Quick-Pill)                     │
│  ───────────────────────────────────────────────────────────────────   │
│  [Home Dashboard]  [Quran Reader]  [Grammar/Tajweed]  [Hifz]  [AI Chat]│
│  ───────────────────────────────────────────────────────────────────   │
│  BottomNavigationBar (Home, Read, Learn, Memorize, AI Assistant)       │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ UI State & User Intents
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        State & ViewModel Layer                         │
├───────────────────┬───────────────────┬───────────────────┬────────────┤
│   HomeViewModel   │   ReaderViewModel │  GrammarViewModel │ AIViewModel│
│  (Daily reflection│  (Ayah list, font │ (Word analysis,   │(Chat turns,│
│   goals & stats)  │   size, bookmarks)│  Tajweed rules)   │ RAG prompt)│
└─────────┬─────────┴─────────┬─────────┴─────────┬─────────┴──────┬─────┘
          │                   │                   │                │
          ▼                   ▼                   ▼                ▼
┌────────────────────────────────────────────────────────────────────────┐
│                            Repository Layer                            │
├────────────────────────────────────────────────────────────────────────┤
│  QuranRepository  •  AudioRepository  •  UserDataRepository  •  GeminiRepo│
└──────┬──────────────────────┬───────────────────────┬────────────┬─────┘
       │                      │                       │            │
       ▼                      ▼                       ▼            ▼
┌──────────────┐      ┌──────────────┐      ┌─────────────────┐ ┌────────┐
│ Pre-bundled  │      │ Audio Player │      │ Room SQLite DB  │ │ Gemini │
│ Surah Yaseen │      │ (MediaPlayer │      │ (Bookmarks,     │ │ REST   │
│ JSON Assets  │      │  CDN Stream) │      │  Notes, Scores) │ │ Client │
└──────────────┘      └──────────────┘      └─────────────────┘ └────────┘
```

### Core Data Models

1. **`Ayah`**:
   - `number: Int` (1–83)
   - `arabicUthmani: String` (Authentic text with diacritics)
   - `translationEn: String` (Sahih International)
   - `translationUr: String` (Fateh Muhammad Jalandhri)
   - `transliteration: String`
   - `words: List<Word>`
   - `audioUrl: String`
   - `tajweedTokens: List<TajweedToken>`

2. **`Word`**:
   - `position: Int`
   - `arabic: String`
   - `translationEn: String`
   - `translationUr: String`
   - `root: String` (جذر)
   - `partOfSpeech: String` (اسم, فعل, حرف)
   - `grammarRole: String` (e.g., مبتدأ, خبر, فاعل, مفعول به)
   - `caseState: String` (مرفوع, منصوب, مجرور)
   - `explanation: String`

3. **`Bookmark & Note Entity`**:
   - `ayahNumber: Int`, `timestamp: Long`, `noteText: String?`, `colorTag: String`

4. **`HifzProgressEntity`**:
   - `ayahNumber: Int`, `status: MemorizationStatus` (NOT_STARTED, LEARNING, MEMORIZED, MASTERED), `reviewCount: Int`, `lastTestedDate: Long`

---

## 5. Implementation Sequence & Plan

1. **Project & Resource Foundation**:
   - Configure `metadata.json` ("Surah Yaseen") and `strings.xml`.
   - Update `app/build.gradle.kts` and `.env.example` with dependencies (Retrofit, Serialization, Room, Icons Extended).
   - Establish theme tokens in `Theme.kt`, `Color.kt`, and `Type.kt` matching the Emerald & Gold aesthetic.
2. **Canonical Data Asset Creation**:
   - Generate complete, source-verified `surah_yaseen.json` containing all 83 ayahs, translations, word roots, and Tajweed annotations.
3. **Database & Persistence Layer**:
   - Implement Room entities, DAOs, and database for bookmarks, notes, quiz scores, and Hifz progress.
   - Implement `QuranRepository` loading from assets and caching into Room.
4. **Audio Playback Manager**:
   - Implement `RecitationPlayerManager` handling play, pause, seek, ayah repetition, repeat-range loops, reciter switching, and sleep timer.
5. **Quran Reader & Translation View**:
   - High-fidelity ayah card list with adjustable Arabic typography, RTL display, word-by-word modal sheet, bookmarking, and audio sync highlighting.
6. **Tajweed & Grammar Learning Screen**:
   - Interactive Tajweed rule visualizer with color legend.
   - Comprehensive Arabic grammar module (Vocabulary, Root Search, Nahw, Sarf, and I'rab breakdown for Surah Yaseen).
7. **Hifz & Memorization Studio**:
   - Progressive concealment screen, tap-to-reveal word-by-word mode, fill-in-the-blank quizzes, and 83-ayah progress visualizer.
8. **Interactive Quizzes & Daily Reflection**:
   - Vocabulary match quiz, root identification quiz, next-ayah recall test.
   - Daily ayah reflection with journal entry saving.
   - Authentic Hadith & Virtues references section with scholarly citations.
9. **Grounded Gemini AI Assistant**:
   - Build Retrofit REST client with system prompt grounding, context injection of Surah Yaseen verses, suggested question chips, and Arabic/Urdu/English support.
10. **Settings, Downloads & Navigation Integration**:
    - Bottom navigation linking Home, Read, Learn, Memorize, and AI Assistant.
    - Settings screen for language, font sizes, audio speeds, reciters, and reading goals.
11. **Verification**:
    - Build and compile check via `compile_applet`.
    - Unit testing of repository, audio state, and quiz logic.
