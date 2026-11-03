package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PurpleAccent,
    onPrimary = Color.White,
    primaryContainer = PurpleDark,
    onPrimaryContainer = TextLight,
    secondary = ArtisanGold,
    onSecondary = PurpleDark,
    secondaryContainer = MidnightSurfaceVariant,
    onSecondaryContainer = SoftGold,
    tertiary = PurpleContainer,
    onTertiary = PurpleDark,
    background = MidnightDark,
    onBackground = TextLight,
    surface = MidnightSurface,
    onSurface = TextLight,
    surfaceVariant = MidnightSurfaceVariant,
    onSurfaceVariant = TextMutedDark,
    outline = Color(0xFF4A3270)
)

private val LightColorScheme = lightColorScheme(
    primary = PurplePrimary,
    onPrimary = PureWhite,
    primaryContainer = PurpleContainer,
    onPrimaryContainer = OnPurpleContainer,
    secondary = ArtisanGold,
    onSecondary = PureWhite,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = PurpleLight,
    onTertiary = PureWhite,
    background = LilacPearlBackground,
    onBackground = TextDark,
    surface = PureWhite,
    onSurface = TextDark,
    surfaceVariant = LavenderCard,
    onSurfaceVariant = TextMuted,
    outline = Color(0xFFD7C9EB)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional royal purple & gold aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
