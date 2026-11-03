package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArtisanGold
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleDark
import com.example.ui.theme.PurplePrimary
import com.example.ui.viewmodel.ConcealmentMode
import com.example.ui.viewmodel.HifzViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HifzScreen(
    viewModel: HifzViewModel,
    onPlayAyahAudio: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val ayah = state.currentAyah

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("hifz_screen")
    ) {
        item {
            Text(
                text = "Surah An-Naba Hifz Studio",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Progressive concealment, tap-to-reveal recall, and 40-ayah milestone tracking.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Stats summary card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.totalMemorized}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PurplePrimary
                        )
                        Text("Memorized", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.totalLearning}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = ArtisanGold
                        )
                        Text("Learning", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${state.totalMastered}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = PurpleDark
                        )
                        Text("Mastered", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${40 - (state.totalMemorized + state.totalLearning + state.totalMastered)}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Text("Remaining", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 40-Ayah Visual Grid Selector (Horizontal scrolling pills)
            Text(
                text = "Select Ayah (1 - 40):",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(40) { idx ->
                    val ayahNum = idx + 1
                    val isSelected = state.currentAyahNumber == ayahNum
                    val status = state.hifzMap[ayahNum]?.status ?: "NOT_STARTED"

                    val pillBorder = if (isSelected) ArtisanGold else Color.Transparent
                    val pillBg = when (status) {
                        "MASTERED" -> PurplePrimary
                        "MEMORIZED" -> PurpleContainer
                        "LEARNING" -> ArtisanGold.copy(alpha = 0.25f)
                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(pillBg, CircleShape)
                            .border(1.5.dp, pillBorder, CircleShape)
                            .clickable { viewModel.selectAyah(ayahNum) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$ayahNum",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (status == "MASTERED") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Concealment Mode Selector
            Text(
                text = "Memorization Practice Mode:",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = state.concealmentMode == ConcealmentMode.FULL_VISIBLE,
                    onClick = { viewModel.setConcealmentMode(ConcealmentMode.FULL_VISIBLE) },
                    label = { Text("Full Text") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = state.concealmentMode == ConcealmentMode.TAP_TO_REVEAL,
                    onClick = { viewModel.setConcealmentMode(ConcealmentMode.TAP_TO_REVEAL) },
                    label = { Text("Tap-to-Reveal") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )

                FilterChip(
                    selected = state.concealmentMode == ConcealmentMode.BLANK_RECALL,
                    onClick = { viewModel.setConcealmentMode(ConcealmentMode.BLANK_RECALL) },
                    label = { Text("Blank Recall") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PurplePrimary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Active Ayah Practice Card
        ayah?.let { currentAyah ->
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ayah ${currentAyah.number} • ${currentAyah.theme}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PurplePrimary
                            )

                            IconButton(onClick = { onPlayAyahAudio(currentAyah.number) }) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Play Ayah", tint = ArtisanGold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Interactive Word Flow based on Concealment Mode
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                currentAyah.words.forEach { word ->
                                    val isRevealed = when (state.concealmentMode) {
                                        ConcealmentMode.FULL_VISIBLE -> true
                                        ConcealmentMode.TAP_TO_REVEAL -> state.revealedWordPositions.contains(word.position)
                                        ConcealmentMode.BLANK_RECALL -> state.revealedWordPositions.contains(word.position)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isRevealed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else ArtisanGold.copy(alpha = 0.15f),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isRevealed) MaterialTheme.colorScheme.outline.copy(alpha = 0.25f) else ArtisanGold
                                        ),
                                        modifier = Modifier
                                            .clickable { viewModel.toggleRevealWord(word.position) }
                                            .padding(2.dp)
                                    ) {
                                        Text(
                                            text = if (isRevealed) word.arabic else "•••",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontFamily = FontFamily.Serif,
                                                fontWeight = FontWeight.Normal
                                            ),
                                            color = if (isRevealed) MaterialTheme.colorScheme.onSurface else ArtisanGold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "﴿${currentAyah.number}﴾",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif),
                                    color = ArtisanGold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reveal All Words & Toggle Translation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { viewModel.revealAllWords() }) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, tint = PurplePrimary)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reveal All Words", color = PurplePrimary)
                            }

                            TextButton(onClick = { viewModel.toggleTranslation() }) {
                                Icon(
                                    if (state.isTranslationRevealed) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null,
                                    tint = ArtisanGold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (state.isTranslationRevealed) "Hide Meaning" else "Show Meaning", color = ArtisanGold)
                            }
                        }

                        // Translation reveal
                        AnimatedVisibility(visible = state.isTranslationRevealed) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                Text(
                                    text = currentAyah.translationEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                    Text(
                                        text = currentAyah.translationUr,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Self-Assessment Buttons
                        Text(
                            text = "Self-Assessment for Ayah ${currentAyah.number}:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.markStatus("LEARNING") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Learning", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { viewModel.markStatus("MEMORIZED") },
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Memorized", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = { viewModel.markStatus("MASTERED") },
                                colors = ButtonDefaults.buttonColors(containerColor = ArtisanGold),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = PurpleDark, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Mastered", color = PurpleDark, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Next / Previous Navigation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.previousAyah() },
                                enabled = currentAyah.number > 1,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Previous Ayah")
                            }

                            Button(
                                onClick = { viewModel.nextAyah() },
                                enabled = currentAyah.number < 40,
                                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Next Ayah")
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
