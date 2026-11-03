package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.audio.AudioRecordingManager
import com.example.audio.RecitationPlayerManager
import com.example.data.repository.GeminiRepository
import com.example.data.repository.QuranRepository
import com.example.data.repository.UserDataRepository
import com.example.ui.components.AudioMiniPlayerBar
import com.example.ui.navigation.Screen
import com.example.ui.navigation.bottomNavScreens
import com.example.ui.screens.AiChatScreen
import com.example.ui.screens.AudioPlayerScreen
import com.example.ui.screens.BookmarksNotesScreen
import com.example.ui.screens.HifzScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.RecitationPracticeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TajweedAndGrammarScreen
import com.example.ui.theme.ArtisanGold
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AiChatViewModel
import com.example.ui.viewmodel.GrammarViewModel
import com.example.ui.viewmodel.HifzViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.QuizViewModel
import com.example.ui.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var quranRepository: QuranRepository
    private lateinit var userDataRepository: UserDataRepository
    private lateinit var geminiRepository: GeminiRepository
    private lateinit var playerManager: RecitationPlayerManager
    private lateinit var recordingManager: AudioRecordingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Repositories & Managers
        quranRepository = QuranRepository(applicationContext)
        userDataRepository = UserDataRepository(applicationContext)
        geminiRepository = GeminiRepository(quranRepository)
        playerManager = RecitationPlayerManager(applicationContext, quranRepository)
        recordingManager = AudioRecordingManager(applicationContext)

        setContent {
            val userPrefs by userDataRepository.userPreferencesFlow.collectAsState(
                initial = com.example.data.model.UserPreferences()
            )

            MyApplicationTheme(
                darkTheme = userPrefs.darkTheme,
                dynamicColor = false
            ) {
                SurahNabaApp(
                    quranRepository = quranRepository,
                    userDataRepository = userDataRepository,
                    geminiRepository = geminiRepository,
                    playerManager = playerManager,
                    recordingManager = recordingManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        playerManager.release()
        recordingManager.stopPlayback()
    }
}

@Composable
fun SurahNabaApp(
    quranRepository: QuranRepository,
    userDataRepository: UserDataRepository,
    geminiRepository: GeminiRepository,
    playerManager: RecitationPlayerManager,
    recordingManager: AudioRecordingManager
) {
    val coroutineScope = rememberCoroutineScope()
    var currentScreen by remember { mutableStateOf<String>(Screen.Home.route) }
    var screenBackStack by remember { mutableStateOf(listOf(Screen.Home.route)) }

    val playerState by playerManager.playerState.collectAsState()
    val userPrefs by userDataRepository.userPreferencesFlow.collectAsState(
        initial = com.example.data.model.UserPreferences()
    )

    // ViewModels
    val homeViewModel = remember { HomeViewModel(quranRepository, userDataRepository) }
    val readerViewModel = remember { ReaderViewModel(quranRepository, userDataRepository) }
    val grammarViewModel = remember { GrammarViewModel(quranRepository) }
    val hifzViewModel = remember { HifzViewModel(quranRepository, userDataRepository) }
    val aiChatViewModel = remember { AiChatViewModel(geminiRepository, quranRepository) }
    val quizViewModel = remember { QuizViewModel(quranRepository, userDataRepository) }

    fun navigateTo(route: String) {
        if (currentScreen != route) {
            screenBackStack = screenBackStack + route
            currentScreen = route
        }
    }

    fun popBack() {
        if (screenBackStack.size > 1) {
            val newStack = screenBackStack.dropLast(1)
            screenBackStack = newStack
            currentScreen = newStack.last()
        } else {
            currentScreen = Screen.Home.route
        }
    }

    // BackHandler for sub-screens
    if (currentScreen != Screen.Home.route) {
        BackHandler {
            popBack()
        }
    }

    val isFullScreenModal = currentScreen == Screen.AudioPlayer.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isFullScreenModal) {
                Column(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    // Audio Mini Player Bar (visible when audio is loaded/playing or paused)
                    if (playerState.currentPositionMs > 0 || playerState.isPlaying || playerState.isLoading) {
                        val currentAyah = quranRepository.getAyah(playerState.currentAyahNumber)
                        AudioMiniPlayerBar(
                            playerState = playerState,
                            currentAyah = currentAyah,
                            onTogglePlayPause = { playerManager.togglePlayPause() },
                            onNextAyah = { playerManager.nextAyah() },
                            onPreviousAyah = { playerManager.previousAyah() },
                            onOpenFullPlayer = { navigateTo(Screen.AudioPlayer.route) }
                        )
                    }

                    // Persistent Bottom Navigation Bar
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        bottomNavScreens.forEach { screen ->
                            val isSelected = currentScreen == screen.route
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { navigateTo(screen.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (isSelected) screen.activeIcon else screen.inactiveIcon,
                                        contentDescription = screen.titleEn
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (userPrefs.appLanguage == "ur") screen.titleUr else screen.titleEn,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PurplePrimary,
                                    selectedTextColor = PurplePrimary,
                                    indicatorColor = PurpleContainer
                                ),
                                modifier = Modifier.testTag("nav_item_${screen.route}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.Home.route -> {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onNavigateTo = { route -> navigateTo(route) },
                        onPlayAyah = { ayahNum ->
                            playerManager.playAyah(ayahNum, userPrefs.selectedReciter)
                            navigateTo(Screen.AudioPlayer.route)
                        },
                        onAskAiAyah = { ayahNum ->
                            aiChatViewModel.setContextAyah(ayahNum)
                            navigateTo(Screen.AiAssistant.route)
                        },
                        isDarkTheme = userPrefs.darkTheme,
                        onToggleTheme = {
                            coroutineScope.launch {
                                userDataRepository.setDarkTheme(!userPrefs.darkTheme)
                            }
                        }
                    )
                }

                Screen.Read.route -> {
                    ReaderScreen(
                        viewModel = readerViewModel,
                        playerState = playerState,
                        onPlayAyah = { ayahNum -> playerManager.playAyah(ayahNum, userPrefs.selectedReciter) },
                        onAskAiAboutAyah = { ayahNum ->
                            aiChatViewModel.setContextAyah(ayahNum)
                            navigateTo(Screen.AiAssistant.route)
                        },
                        onAskAiAboutWord = { query ->
                            aiChatViewModel.sendMessage(query)
                            navigateTo(Screen.AiAssistant.route)
                        },
                        onUpdateArabicFontSize = { size ->
                            coroutineScope.launch { userDataRepository.setArabicFontSize(size) }
                        },
                        onToggleWordByWord = { enabled ->
                            coroutineScope.launch { userDataRepository.toggleWordByWord(enabled) }
                        },
                        onToggleUrdu = { enabled ->
                            coroutineScope.launch { userDataRepository.toggleUrdu(enabled) }
                        },
                        onToggleTransliteration = { enabled ->
                            coroutineScope.launch { userDataRepository.toggleTransliteration(enabled) }
                        }
                    )
                }

                Screen.Learn.route -> {
                    TajweedAndGrammarScreen(
                        viewModel = grammarViewModel,
                        onAskAiQuestion = { q ->
                            aiChatViewModel.sendMessage(q)
                            navigateTo(Screen.AiAssistant.route)
                        }
                    )
                }

                Screen.Memorize.route -> {
                    HifzScreen(
                        viewModel = hifzViewModel,
                        onPlayAyahAudio = { ayahNum -> playerManager.playAyah(ayahNum, userPrefs.selectedReciter) }
                    )
                }

                Screen.AiAssistant.route -> {
                    AiChatScreen(viewModel = aiChatViewModel)
                }

                Screen.Settings.route -> {
                    SettingsScreen(
                        preferences = userPrefs,
                        onUpdateArabicFontSize = { size -> coroutineScope.launch { userDataRepository.setArabicFontSize(size) } },
                        onUpdateTranslationFontSize = { size -> coroutineScope.launch { userDataRepository.setTranslationFontSize(size) } },
                        onToggleTransliteration = { en -> coroutineScope.launch { userDataRepository.toggleTransliteration(en) } },
                        onToggleUrdu = { ur -> coroutineScope.launch { userDataRepository.toggleUrdu(ur) } },
                        onToggleEnglish = { eng -> coroutineScope.launch { userDataRepository.toggleEnglish(eng) } },
                        onToggleWordByWord = { wbw -> coroutineScope.launch { userDataRepository.toggleWordByWord(wbw) } },
                        onSelectReciter = { rec ->
                            coroutineScope.launch { userDataRepository.setReciter(rec) }
                            playerManager.setReciter(rec)
                        },
                        onSetDailyGoal = { goal -> coroutineScope.launch { userDataRepository.setDailyReadingGoal(goal) } },
                        onToggleDarkTheme = { dark -> coroutineScope.launch { userDataRepository.setDarkTheme(dark) } },
                        onToggleEyeComfort = { eye -> coroutineScope.launch { userDataRepository.setEyeComfortMode(eye) } },
                        onNavigateBack = { popBack() }
                    )
                }

                Screen.AudioPlayer.route -> {
                    val currentAyah = quranRepository.getAyah(playerState.currentAyahNumber)
                    AudioPlayerScreen(
                        playerState = playerState,
                        currentAyah = currentAyah,
                        onTogglePlayPause = { playerManager.togglePlayPause() },
                        onSeekTo = { pos -> playerManager.seekTo(pos) },
                        onNextAyah = { playerManager.nextAyah() },
                        onPreviousAyah = { playerManager.previousAyah() },
                        onSelectReciter = { rec ->
                            coroutineScope.launch { userDataRepository.setReciter(rec) }
                            playerManager.setReciter(rec)
                        },
                        onSetSpeed = { speed -> playerManager.setPlaybackSpeed(speed) },
                        onSetRepeat = { count -> playerManager.setRepeatCount(count) },
                        onSetSleepTimer = { mins -> playerManager.setSleepTimer(mins) },
                        onNavigateBack = { popBack() }
                    )
                }

                Screen.Bookmarks.route -> {
                    BookmarksNotesScreen(
                        quranRepository = quranRepository,
                        userDataRepository = userDataRepository,
                        onNavigateToAyah = { ayahNum ->
                            readerViewModel.updateActiveAyah(ayahNum)
                            navigateTo(Screen.Read.route)
                        },
                        onNavigateBack = { popBack() }
                    )
                }

                Screen.Quiz.route -> {
                    QuizScreen(
                        viewModel = quizViewModel,
                        onNavigateBack = { popBack() }
                    )
                }

                Screen.Practice.route -> {
                    RecitationPracticeScreen(
                        quranRepository = quranRepository,
                        recordingManager = recordingManager,
                        onPlayReferenceAudio = { ayahNum -> playerManager.playAyah(ayahNum, userPrefs.selectedReciter) },
                        onNavigateBack = { popBack() }
                    )
                }
            }

            // Eye Comfort Night Filter overlay
            if (userPrefs.eyeComfortMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFFFFA000).copy(alpha = 0.08f))
                )
            }
        }
    }
}
