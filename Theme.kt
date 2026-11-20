package com.littlecare.babytracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val OledDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF6C87A),          // Warm Amber Gold
    onPrimary = Color(0xFF1A1205),
    primaryContainer = Color(0xFF2B2418),
    onPrimaryContainer = Color(0xFFFFE5B8),
    secondary = Color(0xFF7DD3FC),        // Calm Sky Blue
    onSecondary = Color(0xFF062030),
    secondaryContainer = Color(0xFF152838),
    onSecondaryContainer = Color(0xFFD6F1FF),
    tertiary = Color(0xFF86EFAC),         // Soft Mint Green
    onTertiary = Color(0xFF072614),
    background = Color(0xFF000000),       // Pure OLED Black
    onBackground = Color(0xFFF3F4F6),
    surface = Color(0xFF0E121B),          // Elevated Dark Card
    onSurface = Color(0xFFF3F4F6),
    surfaceVariant = Color(0xFF171E2C),
    onSurfaceVariant = Color(0xFF9CA3AF),
    outline = Color(0xFF2A3447),
    error = Color(0xFFF87171)
)

private val NightVisionColorScheme = darkColorScheme(
    primary = Color(0xFFE05244),          // Low-glare warm crimson/amber for 3 AM feeds
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF260D0B),
    onPrimaryContainer = Color(0xFFFFB4AB),
    secondary = Color(0xFFD97736),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF241307),
    onSecondaryContainer = Color(0xFFFFDDBF),
    tertiary = Color(0xFFB85C4F),
    onTertiary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFD4887E),
    surface = Color(0xFF0B0505),
    onSurface = Color(0xFFD4887E),
    surfaceVariant = Color(0xFF170B0B),
    onSurfaceVariant = Color(0xFF9E5D55),
    outline = Color(0xFF331715),
    error = Color(0xFFFF6B6B)
)

@Composable
fun LittleCareTheme(
    nightVisionMode: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (nightVisionMode) NightVisionColorScheme else OledDarkColorScheme
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
