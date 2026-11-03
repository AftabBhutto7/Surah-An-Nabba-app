package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.PlayerState
import com.example.data.model.Ayah
import com.example.data.model.Reciter
import com.example.ui.theme.ArtisanGold
import com.example.ui.theme.PurpleDark
import com.example.ui.theme.PurplePrimary
import com.example.ui.theme.SoftGold

@Composable
fun AudioPlayerScreen(
    playerState: PlayerState,
    currentAyah: Ayah?,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onNextAyah: () -> Unit,
    onPreviousAyah: () -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetRepeat: (Int) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showReciterMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }
    var showSleepMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(PurpleDark, MaterialTheme.colorScheme.background)
                )
            )
            .padding(20.dp)
            .testTag("audio_player_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = SoftGold)
            }
            Text(
                text = "Recitation Player",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimary
            )
            // Sleep Timer Icon
            Box {
                IconButton(onClick = { showSleepMenu = true }) {
                    Icon(
                        Icons.Filled.Bedtime,
                        contentDescription = "Sleep Timer",
                        tint = if (playerState.sleepTimerMinutesLeft > 0) ArtisanGold else SoftGold
                    )
                }
                DropdownMenu(expanded = showSleepMenu, onDismissRequest = { showSleepMenu = false }) {
                    DropdownMenuItem(text = { Text("Off") }, onClick = { onSetSleepTimer(0); showSleepMenu = false })
                    DropdownMenuItem(text = { Text("15 minutes") }, onClick = { onSetSleepTimer(15); showSleepMenu = false })
                    DropdownMenuItem(text = { Text("30 minutes") }, onClick = { onSetSleepTimer(30); showSleepMenu = false })
                    DropdownMenuItem(text = { Text("45 minutes") }, onClick = { onSetSleepTimer(45); showSleepMenu = false })
                    DropdownMenuItem(text = { Text("60 minutes") }, onClick = { onSetSleepTimer(60); showSleepMenu = false })
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Center Artwork / Ayah Banner Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(ArtisanGold.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${playerState.currentAyahNumber}",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = ArtisanGold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Surah An-Naba • سُورَة النَّبَأ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PurplePrimary
                )

                currentAyah?.let { ayah ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Ayah ${ayah.number} • ${ayah.theme}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            text = ayah.arabicUthmani,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontFamily = FontFamily.Serif,
                                lineHeight = 36.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = ayah.translationEn,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 3
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Reciter Selection Pill & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reciter dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.clickable { showReciterMenu = true }
                ) {
                    Text(
                        text = playerState.reciter.displayNameEn,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = PurplePrimary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
                DropdownMenu(expanded = showReciterMenu, onDismissRequest = { showReciterMenu = false }) {
                    Reciter.values().forEach { reciter ->
                        DropdownMenuItem(
                            text = { Text("${reciter.displayNameEn} (${reciter.displayNameUr})") },
                            onClick = {
                                onSelectReciter(reciter)
                                showReciterMenu = false
                            }
                        )
                    }
                }
            }

            // Speed & Repeat buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box {
                    IconButton(onClick = { showSpeedMenu = true }) {
                        Icon(Icons.Filled.Speed, contentDescription = "Speed", tint = ArtisanGold)
                    }
                    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                        listOf(0.75f, 1.0f, 1.25f, 1.5f).forEach { speed ->
                            DropdownMenuItem(
                                text = { Text("${speed}x") },
                                onClick = { onSetSpeed(speed); showSpeedMenu = false }
                            )
                        }
                    }
                }

                Box {
                    IconButton(onClick = { showRepeatMenu = true }) {
                        Icon(Icons.Filled.Repeat, contentDescription = "Repeat", tint = ArtisanGold)
                    }
                    DropdownMenu(expanded = showRepeatMenu, onDismissRequest = { showRepeatMenu = false }) {
                        listOf(1, 3, 5, 10).forEach { rCount ->
                            DropdownMenuItem(
                                text = { Text(if (rCount == 1) "Play Once (1x)" else "Repeat $rCount times") },
                                onClick = { onSetRepeat(rCount); showRepeatMenu = false }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Full Surah Seek Bar (smooth continuous progress across all 40 Ayahs)
        val currentSurahMs = playerState.surahCurrentPositionMs.toFloat()
        val totalSurahMs = playerState.surahTotalDurationMs.toFloat().coerceAtLeast(1f)

        Slider(
            value = currentSurahMs.coerceIn(0f, totalSurahMs),
            onValueChange = { onSeekTo(it.toInt()) },
            valueRange = 0f..totalSurahMs,
            colors = SliderDefaults.colors(
                thumbColor = ArtisanGold,
                activeTrackColor = ArtisanGold,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("audio_surah_slider")
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatDuration(playerState.surahCurrentPositionMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "Surah An-Naba • Ayah ${playerState.currentAyahNumber}/40",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = ArtisanGold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
            Text(
                text = formatDuration(playerState.surahTotalDurationMs),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.85f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transport Buttons (Prev, Play/Pause, Next)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPreviousAyah,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous Ayah", tint = PurplePrimary, modifier = Modifier.size(34.dp))
            }

            if (playerState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(68.dp),
                    color = ArtisanGold
                )
            } else {
                IconButton(
                    onClick = onTogglePlayPause,
                    modifier = Modifier
                        .size(72.dp)
                        .background(ArtisanGold, CircleShape)
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = PurpleDark,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            IconButton(
                onClick = onNextAyah,
                modifier = Modifier.size(52.dp)
            ) {
                Icon(Icons.Filled.SkipNext, contentDescription = "Next Ayah", tint = PurplePrimary, modifier = Modifier.size(34.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun formatDuration(ms: Int): String {
    val totalSec = ms / 1000
    val minutes = totalSec / 60
    val seconds = totalSec % 60
    return String.format("%02d:%02d", minutes, seconds)
}
