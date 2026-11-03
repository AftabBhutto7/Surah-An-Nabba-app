package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlayerState
import com.example.ui.components.AyahCard
import com.example.ui.components.WordDetailSheet
import com.example.ui.theme.ArtisanGold
import com.example.ui.theme.PurplePrimary
import com.example.ui.viewmodel.ReaderViewModel
import kotlinx.coroutines.launch

@Composable
fun ReaderScreen(
    viewModel: ReaderViewModel,
    playerState: PlayerState,
    onPlayAyah: (Int) -> Unit,
    onAskAiAboutAyah: (Int) -> Unit,
    onAskAiAboutWord: (String) -> Unit,
    onUpdateArabicFontSize: (Float) -> Unit,
    onToggleWordByWord: (Boolean) -> Unit,
    onToggleUrdu: (Boolean) -> Unit,
    onToggleTransliteration: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    var showJumpDialog by remember { mutableStateOf(false) }
    var jumpInput by remember { mutableStateOf("") }
    var showQuickFormattingBar by remember { mutableStateOf(false) }

    // Auto-scroll when active ayah changes via audio player
    LaunchedEffect(playerState.currentAyahNumber) {
        if (playerState.isPlaying && state.searchQuery.isBlank()) {
            val targetIndex = (playerState.currentAyahNumber - 1).coerceIn(0, state.ayahs.size - 1)
            listState.animateScrollToItem(targetIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("reader_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Reader Control & Search Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text("Search Ayah, keyword, Urdu or root...") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Filled.Search, contentDescription = "Search", tint = PurplePrimary)
                            },
                            trailingIcon = {
                                if (state.searchQuery.isNotBlank()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reader_search_input")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Jump to Ayah button
                        IconButton(
                            onClick = { showJumpDialog = true },
                            modifier = Modifier.testTag("jump_to_ayah_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Navigation,
                                contentDescription = "Jump to Ayah",
                                tint = ArtisanGold
                            )
                        }

                        // Formatting toggle button
                        IconButton(
                            onClick = { showQuickFormattingBar = !showQuickFormattingBar },
                            modifier = Modifier.testTag("toggle_formatting_bar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FormatSize,
                                contentDescription = "Formatting",
                                tint = PurplePrimary
                            )
                        }
                    }

                    // Expandable quick formatting and filter bar
                    AnimatedVisibility(visible = showQuickFormattingBar) {
                        Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = state.preferences.showWordByWord,
                                    onClick = { onToggleWordByWord(!state.preferences.showWordByWord) },
                                    label = { Text("Word-by-Word") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )

                                FilterChip(
                                    selected = state.preferences.showUrduTranslation,
                                    onClick = { onToggleUrdu(!state.preferences.showUrduTranslation) },
                                    label = { Text("Urdu") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )

                                FilterChip(
                                    selected = state.preferences.showTransliteration,
                                    onClick = { onToggleTransliteration(!state.preferences.showTransliteration) },
                                    label = { Text("Transliteration") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PurplePrimary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Arabic Font Size slider
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Arabic Size: ${state.preferences.arabicFontSize.toInt()}sp",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.width(110.dp)
                                )
                                Slider(
                                    value = state.preferences.arabicFontSize,
                                    onValueChange = { onUpdateArabicFontSize(it) },
                                    valueRange = 22f..42f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = ArtisanGold,
                                        activeTrackColor = PurplePrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Surah An-Naba Ayah List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("ayah_lazy_column")
            ) {
                // Surah Bismillah Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = ArtisanGold
                        )
                    }
                }

                items(state.filteredAyahs, key = { it.number }) { ayah ->
                    AyahCard(
                        ayah = ayah,
                        isCurrentPlaying = playerState.currentAyahNumber == ayah.number,
                        isPlaying = playerState.isPlaying,
                        isBookmarked = state.bookmarksMap.containsKey(ayah.number),
                        preferences = state.preferences,
                        onPlayAyah = onPlayAyah,
                        onToggleBookmark = { num -> viewModel.toggleBookmark(num) },
                        onWordClick = { word, selectedAyah ->
                            viewModel.selectWordDetail(word, selectedAyah)
                        },
                        onAskAiAboutAyah = onAskAiAboutAyah
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }
        }

        // Word Detail Bottom Sheet
        if (state.selectedWordForDetail != null && state.selectedAyahForWord != null) {
            WordDetailSheet(
                word = state.selectedWordForDetail,
                ayah = state.selectedAyahForWord,
                onDismiss = { viewModel.selectWordDetail(null, null) },
                onAskAiAboutWord = { word, ayah ->
                    val query = "Please explain the Arabic word '${word.arabic}' (Root: ${word.root}, Grammar: ${word.grammarRole}) in Surah An-Naba 78:${ayah.number}."
                    viewModel.selectWordDetail(null, null)
                    onAskAiAboutWord(query)
                }
            )
        }

        // Jump to Ayah Dialog
        if (showJumpDialog) {
            AlertDialog(
                onDismissRequest = { showJumpDialog = false },
                title = { Text("Jump to Ayah") },
                text = {
                    Column {
                        Text("Enter Ayah number between 1 and ${state.ayahs.size.coerceAtLeast(40)}:")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = jumpInput,
                            onValueChange = { jumpInput = it.filter { char -> char.isDigit() } },
                            placeholder = { Text("e.g. 1") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val num = jumpInput.toIntOrNull()
                            val totalAyahs = state.ayahs.size.coerceAtLeast(40)
                            if (num != null && num in 1..totalAyahs) {
                                coroutineScope.launch {
                                    listState.scrollToItem(num - 1)
                                }
                                viewModel.updateActiveAyah(num)
                            }
                            showJumpDialog = false
                            jumpInput = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                    ) {
                        Text("Go")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showJumpDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
