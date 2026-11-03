package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Reciter
import com.example.data.model.UserPreferences
import com.example.ui.theme.ArtisanGold
import com.example.ui.theme.PurplePrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferences: UserPreferences,
    onUpdateArabicFontSize: (Float) -> Unit,
    onUpdateTranslationFontSize: (Float) -> Unit,
    onToggleTransliteration: (Boolean) -> Unit,
    onToggleUrdu: (Boolean) -> Unit,
    onToggleEnglish: (Boolean) -> Unit,
    onToggleWordByWord: (Boolean) -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onSetDailyGoal: (Int) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleEyeComfort: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var reciterExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Settings & Preferences",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Audio & Recitation Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reciter & Audio",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ExposedDropdownMenuBox(
                        expanded = reciterExpanded,
                        onExpandedChange = { reciterExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${preferences.selectedReciter.displayNameEn} (${preferences.selectedReciter.displayNameUr})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Default Reciter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reciterExpanded) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = reciterExpanded,
                            onDismissRequest = { reciterExpanded = false }
                        ) {
                            Reciter.values().forEach { reciter ->
                                DropdownMenuItem(
                                    text = { Text("${reciter.displayNameEn} (${reciter.displayNameUr})") },
                                    onClick = {
                                        onSelectReciter(reciter)
                                        reciterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Typography Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Typography & Display Size",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Arabic Quran Script Size: ${preferences.arabicFontSize.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = preferences.arabicFontSize,
                        onValueChange = onUpdateArabicFontSize,
                        valueRange = 22f..42f,
                        colors = SliderDefaults.colors(thumbColor = ArtisanGold, activeTrackColor = PurplePrimary)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Translation Font Size: ${preferences.translationFontSize.toInt()} sp",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = preferences.translationFontSize,
                        onValueChange = onUpdateTranslationFontSize,
                        valueRange = 13f..24f,
                        colors = SliderDefaults.colors(thumbColor = ArtisanGold, activeTrackColor = PurplePrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Reading Content Toggles Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Reader Modules & Translations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingToggleRow(
                        title = "Word-by-Word Breakdown",
                        subtitle = "Interactive chips with root & grammatical roles",
                        checked = preferences.showWordByWord,
                        onCheckedChange = onToggleWordByWord
                    )

                    SettingToggleRow(
                        title = "Urdu Translation (اردو ترجمہ)",
                        subtitle = "Fateh Muhammad Jalandhri",
                        checked = preferences.showUrduTranslation,
                        onCheckedChange = onToggleUrdu
                    )

                    SettingToggleRow(
                        title = "English Translation",
                        subtitle = "Sahih International",
                        checked = preferences.showEnglishTranslation,
                        onCheckedChange = onToggleEnglish
                    )

                    SettingToggleRow(
                        title = "Phonetic Transliteration",
                        subtitle = "Latin phonetic guide for pronunciation",
                        checked = preferences.showTransliteration,
                        onCheckedChange = onToggleTransliteration
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Appearance & Reading Goals
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Appearance & Goals",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurplePrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingToggleRow(
                        title = "Dark Theme",
                        subtitle = "Midnight Eclipse & Royal Amethyst purple dark mode",
                        checked = preferences.darkTheme,
                        onCheckedChange = onToggleDarkTheme
                    )

                    SettingToggleRow(
                        title = "Eye-Comfort Night Mode",
                        subtitle = "Warm tone filter for night recitation",
                        checked = preferences.eyeComfortMode,
                        onCheckedChange = onToggleEyeComfort
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Daily Reading Target: ${preferences.dailyReadingGoalAyahs} Ayahs / day",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = preferences.dailyReadingGoalAyahs.toFloat().coerceIn(5f, 40f),
                        onValueChange = { onSetDailyGoal(it.toInt()) },
                        valueRange = 5f..40f,
                        steps = 35,
                        colors = SliderDefaults.colors(thumbColor = ArtisanGold, activeTrackColor = PurplePrimary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Offline & License Status
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Data & Sources",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurplePrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Quran Text: Quran.com verified Uthmani script (King Fahd Complex standard).\n• English: Sahih International.\n• Urdu: Fateh Muhammad Jalandhri.\n• Audio Recitations: EveryAyah CDN (licensed open Quran audio).\n• 100% of all 40 ayahs of Surah An-Naba are cached locally for full offline reading.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = ArtisanGold, checkedTrackColor = PurplePrimary)
        )
    }
}
