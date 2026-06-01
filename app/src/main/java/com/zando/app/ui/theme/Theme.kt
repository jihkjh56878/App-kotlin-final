package com.zando.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ─── Colours ──────────────────────────────────────────────────────────────────

val ZandoGreen     = Color(0xFF3D6B4F)   // primary brand green
val ZandoGreenDark = Color(0xFF2A4D37)
val ZandoGreenLight= Color(0xFFE8F0EB)
val ZandoAccent    = Color(0xFF8B4513)   // warm brown accent
val ZandoDark      = Color(0xFF1A1A1A)
val ZandoGrey      = Color(0xFF6B6B6B)
val ZandoBorder    = Color(0xFFE0E0E0)
val ZandoSurface   = Color(0xFFFFFFFF)
val ZandoBg        = Color(0xFFF8F8F6)
val ZandoSaleBadge = Color(0xFFCC3333)
val ZandoNewBadge  = Color(0xFF3D6B4F)

private val LightColorScheme = lightColorScheme(
    primary          = ZandoGreen,
    onPrimary        = Color.White,
    primaryContainer = ZandoGreenLight,
    secondary        = ZandoAccent,
    onSecondary      = Color.White,
    background       = ZandoBg,
    onBackground     = ZandoDark,
    surface          = ZandoSurface,
    onSurface        = ZandoDark,
    outline          = ZandoBorder,
    error            = ZandoSaleBadge,
)

private val DarkColorScheme = darkColorScheme(
    primary          = Color(0xFF6BAF83),
    onPrimary        = Color.Black,
    primaryContainer = Color(0xFF2A4D37),
    secondary        = Color(0xFFCB9B7A),
    onSecondary      = Color.Black,
    background       = Color(0xFF121212),
    onBackground     = Color(0xFFE8E8E8),
    surface          = Color(0xFF1E1E1E),
    onSurface        = Color(0xFFE8E8E8),
    outline          = Color(0xFF3A3A3A),
    error            = Color(0xFFFF6B6B),
)

// ─── Typography ───────────────────────────────────────────────────────────────

val ZandoTypography = Typography(
    headlineLarge  = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold,   letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
    headlineSmall  = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.SemiBold),
    titleLarge     = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    titleMedium    = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge      = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Normal),
    bodyMedium     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal),
    bodySmall      = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal),
    labelLarge     = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp),
    labelSmall     = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium,   letterSpacing = 0.5.sp),
)

// ─── Theme Entry Point ────────────────────────────────────────────────────────

@Composable
fun ZandoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = ZandoTypography,
        content     = content
    )
}
