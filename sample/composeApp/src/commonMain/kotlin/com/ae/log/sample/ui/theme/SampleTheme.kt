package com.ae.log.sample.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────────────────────────
private val Brand = Color(0xFF000000)
private val BrandLight = Color(0xFFE0E0E0)
private val BrandDark = Color(0xFF121212)

private val DarkScheme =
    darkColorScheme(
        primary = Brand,
        onPrimary = Color.White,
        primaryContainer = BrandDark,
        onPrimaryContainer = Color(0xFFE0E0E0),
        secondary = Color(0xFF7B848A),
        background = Color(0xFF111318),
        surface = Color(0xFF1A1D22),
        surfaceVariant = Color(0xFF252830),
        onBackground = Color(0xFFE2E2E6),
        onSurface = Color(0xFFE2E2E6),
        onSurfaceVariant = Color(0xFF9EA5AF),
        error = Color(0xFFFF6B6B),
    )

private val LightScheme =
    lightColorScheme(
        primary = Brand,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0E0E0),
        onPrimaryContainer = BrandDark,
        secondary = Color(0xFF424242),
        background = Color(0xFFFAFAFA),
        surface = Color.White,
        surfaceVariant = Color(0xFFF5F5F5),
        onBackground = Color(0xFF121212),
        onSurface = Color(0xFF121212),
        onSurfaceVariant = Color(0xFF424242),
        error = Color(0xFFD32F2F),
    )

@Composable
fun SampleTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content,
    )
}
