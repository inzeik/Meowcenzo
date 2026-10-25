package com.example.meowcenzo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Meowcenzo design tokens.
 * Signature: everything that can be tapped is a rounded RECTANGLE ("tile"), never a circle.
 * Surfaces are an aubergine-tinted black; coral to lilac is the only accent pair.
 */
object Meow {
    val Ink = Color(0xFF0F0D12)
    val Panel = Color(0xFF181520)
    val PanelHigh = Color(0xFF241F2F)
    val Line = Color(0xFF332D40)
    val Coral = Color(0xFFFF6B57)
    val Lilac = Color(0xFFB69CFF)
    val TextHi = Color(0xFFF6F2FA)
    val TextLo = Color(0xFF9D96AB)

    val gradient: Brush = Brush.horizontalGradient(listOf(Coral, Lilac))

    val TileSmall = RoundedCornerShape(10.dp)
    val Tile = RoundedCornerShape(14.dp)
    val TileLarge = RoundedCornerShape(24.dp)
}

private val Scheme = darkColorScheme(
    primary = Meow.Coral,
    onPrimary = Meow.Ink,
    secondary = Meow.Lilac,
    onSecondary = Meow.Ink,
    background = Meow.Ink,
    onBackground = Meow.TextHi,
    surface = Meow.Panel,
    onSurface = Meow.TextHi,
    surfaceVariant = Meow.PanelHigh,
    onSurfaceVariant = Meow.TextLo,
    outline = Meow.Line,
)

private val MeowType = Typography(
    headlineMedium = TextStyle(fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, letterSpacing = (-0.8).sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = (-0.2).sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp),
)

@Composable
fun MeowcenzoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Scheme,
        typography = MeowType,
        shapes = Shapes(
            small = Meow.TileSmall,
            medium = Meow.Tile,
            large = Meow.TileLarge,
        ),
        content = content,
    )
}
