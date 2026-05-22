package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentNeonMint,
    onPrimary = UnderAccentTextBlue,
    secondary = ColorSleep,
    background = DeepSlateBlack,
    surface = SurfaceCardDark,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = ActiveBorderGray,
    onSurfaceVariant = TextSecondaryMuted,
    outline = ColorNone
)

private val LightColorScheme = lightColorScheme(
    primary = ColorProductive,
    onPrimary = Color.White,
    secondary = ColorSleep,
    background = LightBg,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightBorder,
    onSurfaceVariant = LightTextSecondary,
    outline = ColorNone
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Defaults to beautiful dark mode
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
