package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Ayah
import com.example.data.model.UserPreferences
import com.example.data.model.Word
import com.example.ui.theme.ArtisanGold
import com.example.ui.theme.PurpleContainer
import com.example.ui.theme.PurpleLight
import com.example.ui.theme.PurplePrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AyahCard(
    ayah: Ayah,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    preferences: UserPreferences,
    onPlayAyah: (Int) -> Unit,
    onToggleBookmark: (Int) -> Unit,
    onWordClick: (Word, Ayah) -> Unit,
    onAskAiAboutAyah: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val containerBg by animateColorAsState(
        targetValue = if (isCurrentPlaying) PurpleContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
        label = "ayah_bg"
    )

    val borderStroke = if (isCurrentPlaying) {
        BorderStroke(1.5.dp, ArtisanGold)
    } else {
        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("ayah_card_${ayah.number}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = borderStroke,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentPlaying) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Ayah Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gold Ayah Number Rosette
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(ArtisanGold.copy(alpha = 0.18f), CircleShape)
                        .border(1.dp, ArtisanGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${ayah.number}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = ArtisanGold
                    )
                }

                // Theme badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "36:${ayah.number} • ${ayah.theme}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }

                // Play / Pause Icon Button
                IconButton(
                    onClick = { onPlayAyah(ayah.number) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("play_ayah_button_${ayah.number}")
                ) {
                    Icon(
                        imageVector = if (isCurrentPlaying && isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Recite Ayah ${ayah.number}",
                        tint = if (isCurrentPlaying) ArtisanGold else PurplePrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canonical Arabic Quran Text (RTL)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Text(
                    text = "${ayah.arabicUthmani} ﴿${ayah.number}﴾",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = preferences.arabicFontSize.sp,
                        lineHeight = (preferences.arabicFontSize * 1.75f).sp,
                        fontWeight = FontWeight.Normal,
                        fontFamily = FontFamily.Serif
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("arabic_text_${ayah.number}")
                )
            }

            // Word-by-Word interactive chip section (if enabled)
            if (preferences.showWordByWord && ayah.words.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ayah.words.forEach { word ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .clickable { onWordClick(word, ayah) }
                                    .testTag("word_chip_${ayah.number}_${word.position}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = word.arabic,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (word.translationEn.isNotBlank()) {
                                        Text(
                                            text = word.translationEn,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Transliteration
            if (preferences.showTransliteration && ayah.transliteration.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = ayah.transliteration,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // English Translation (Sahih International)
            if (preferences.showEnglishTranslation && ayah.translationEn.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ayah.translationEn,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = preferences.translationFontSize.sp,
                        lineHeight = (preferences.translationFontSize * 1.4f).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Urdu Translation (Fateh Muhammad Jalandhri)
            if (preferences.showUrduTranslation && ayah.translationUr.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = ayah.translationUr,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = (preferences.translationFontSize + 1f).sp,
                            lineHeight = ((preferences.translationFontSize + 1f) * 1.5f).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ask AI about this Ayah
                IconButton(
                    onClick = { onAskAiAboutAyah(ayah.number) },
                    modifier = Modifier.testTag("ai_ask_ayah_${ayah.number}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = "Ask AI about Ayah ${ayah.number}",
                        tint = ArtisanGold
                    )
                }

                // Copy Text
                IconButton(
                    onClick = {
                        val textToCopy = "${ayah.arabicUthmani}\n\n${ayah.translationEn}\n\n${ayah.translationUr}\n[Surah An-Naba 78:${ayah.number}]"
                        clipboardManager.setText(AnnotatedString(textToCopy))
                        Toast.makeText(context, "Ayah ${ayah.number} copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_ayah_${ayah.number}")
                ) {
                    Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy Ayah",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Bookmark Toggle
                IconButton(
                    onClick = { onToggleBookmark(ayah.number) },
                    modifier = Modifier.testTag("bookmark_ayah_${ayah.number}")
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove Bookmark" else "Bookmark Ayah",
                        tint = if (isBookmarked) ArtisanGold else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
