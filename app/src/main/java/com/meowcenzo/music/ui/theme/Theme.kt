package com.meowcenzo.music.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// --- STRICT MONOCHROME PALETTE ---
val MonoWhite = Color(0xFFFFFFFF)
val MonoBlack = Color(0xFF000000) // OLED Black
val MonoSurface = Color(0xFF1C1C1E) // Dark Gray panels
val MonoGray = Color(0xFF8E8E93) // Secondary text
val MonoBorder = Color(0xFF38383A) // Subtle borders

object Meow {
    val Coral = MonoWhite // Accent is now White
    val Ink = MonoBlack
    val PanelHigh = MonoSurface
    val Panel = MonoSurface
    val TextHi = MonoWhite
    val TextLo = MonoGray
    val Line = MonoBorder
    val Lilac = MonoGray // Kept for other screens

    val gradient = Brush.linearGradient(listOf(Color(0xFF2C2C2E), MonoBlack))

    val Tile = RoundedCornerShape(0.dp)
    val TileSmall = RoundedCornerShape(0.dp)
    val TileLarge = RoundedCornerShape(0.dp)
}

// --- BOLD SANS-SERIF FONT ---
private val LabTypography = Typography(
    displayLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
    displayMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
    displaySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
    headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
    headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Black),
    headlineSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
)

private val LabColorScheme = darkColorScheme(
    primary = MonoWhite,
    background = MonoBlack,
    surface = MonoSurface,
    onPrimary = MonoBlack,
    onBackground = MonoWhite,
    onSurface = MonoWhite
)

@Composable
fun MeowcenzoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LabColorScheme,
        typography = LabTypography,
        content = content
    )
}