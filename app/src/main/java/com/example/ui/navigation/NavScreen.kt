package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.School
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val titleEn: String,
    val titleUr: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Home : Screen("home", "Home", "ہوم", Icons.Filled.Home, Icons.Outlined.Home)
    object Read : Screen("read", "Read", "تلاوت", Icons.Filled.Book, Icons.Outlined.Book)
    object Learn : Screen("learn", "Learn", "تعلیم", Icons.Filled.School, Icons.Outlined.School)
    object Memorize : Screen("memorize", "Hifz", "حفظ", Icons.Filled.Psychology, Icons.Outlined.Psychology)
    object AiAssistant : Screen("ai_assistant", "AI Tutor", "معاون", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)

    // Sub-screens
    object Settings : Screen("settings", "Settings", "ترتیبات", Icons.Filled.Home, Icons.Outlined.Home)
    object AudioPlayer : Screen("audio_player", "Recitation Player", "صوتی تلاوت", Icons.Filled.Home, Icons.Outlined.Home)
    object Bookmarks : Screen("bookmarks", "Bookmarks & Notes", "نشانی اور نوٹس", Icons.Filled.Home, Icons.Outlined.Home)
    object Quiz : Screen("quiz", "Quiz & Revision", "کوئز", Icons.Filled.Home, Icons.Outlined.Home)
    object Practice : Screen("practice", "Recitation Practice", "مشقِ تلاوت", Icons.Filled.Home, Icons.Outlined.Home)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Read,
    Screen.Learn,
    Screen.Memorize,
    Screen.AiAssistant
)
