package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CodAccentGold,
    onPrimary = Color.Black,
    secondary = CodSurfaceLight,
    onSecondary = CodTextPrimary,
    tertiary = CodAccentRed,
    background = CodDarkBg,
    onBackground = CodTextPrimary,
    surface = CodSurface,
    onSurface = CodTextPrimary,
    surfaceVariant = CodSurfaceLight,
    onSurfaceVariant = CodTextSecondary,
    outline = CodDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for COD gaming look
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
