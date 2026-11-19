package com.littlecare.babytracker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Luxury Obsidian & Champagne OLED Palette — High Contrast & Night-Feed Friendly
val OledBlack = Color(0xFF040507)
val OledSurface = Color(0xFF0D1015)
val OledSurfaceElevated = Color(0xFF151922)
val OledBorder = Color(0xFF222834)
val OledBorderHighlight = Color(0xFF343D4F)

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

// Signature Luxury Champagne Gold + Calm Luminous Semantic Accents
val AccentGold = Color(0xFFF5D08A)
val AccentFeed = Color(0xFF56CCF2)
val AccentPoop = Color(0xFFFBBF24)
val AccentPee = Color(0xFF4ADE80)
val AccentVaccine = Color(0xFFC084FC)
val AccentOverdue = Color(0xFFFB7185)

private val OledDarkColorScheme = darkColorScheme(
    primary = AccentGold,
    onPrimary = OledBlack,
    secondary = AccentFeed,
    onSecondary = OledBlack,
    background = OledBlack,
    onBackground = TextPrimary,
    surface = OledSurface,
    onSurface = TextPrimary,
    surfaceVariant = OledSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = OledBorder,
    error = AccentOverdue,
    onError = OledBlack
)

val AccessibleTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 27.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.4).sp,
        color = TextPrimary
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.2).sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp
    )
)

@Composable
fun LittleCareTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = OledDarkColorScheme,
        typography = AccessibleTypography,
        content = content
    )
}
