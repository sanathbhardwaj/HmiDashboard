package com.example.hmidashboard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object HmiColors {
    val Background = Color(0xFF0A0D10)
    val SurfaceDark = Color(0xFF13181F)
    val SurfaceCard = Color(0xFF181F28)
    val SurfaceCardBorder = Color(0xFF252F3D)
    val Track = Color(0xFF202731)

    val TextPrimary = Color(0xFFF2F5F8)
    val TextSecondary = Color(0xFF8692A3)
    val TextMuted = Color(0xFF505A69)

    val Green = Color(0xFF00E676)
    val GreenDim = Color(0xFF0E3823)
    val GreenGlow = Color(0x3300E676)

    val Cyan = Color(0xFF00E5FF)
    val CyanGlow = Color(0x3300E5FF)

    val Orange = Color(0xFFFF7043)
    val OrangeGlow = Color(0x40FF7043)

    val Red = Color(0xFFFF334B)
    val RedDim = Color(0xFF381216)
    val RedGlow = Color(0x4DFF334B)

    val Amber = Color(0xFFFFB300)
    val Grey = Color(0xFF5C6573)
}

@Composable
fun HmiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            background = HmiColors.Background,
            surface = HmiColors.Background,
            onBackground = HmiColors.TextPrimary,
            onSurface = HmiColors.TextPrimary,
        ),
        content = content,
    )
}
