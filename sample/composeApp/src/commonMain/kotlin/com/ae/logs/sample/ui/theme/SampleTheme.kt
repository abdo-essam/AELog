package com.ae.logs.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Brand colours ─────────────────────────────────────────────────────────────
private val Brand = Color(0xFFBF3547)
private val BrandLight = Color(0xFFFF7166)
private val BrandDark = Color(0xFF8B0020)

private val DarkScheme =
    darkColorScheme(
        primary = Brand,
        onPrimary = Color.White,
        primaryContainer = BrandDark,
        onPrimaryContainer = Color(0xFFFFDAD8),
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
        primaryContainer = Color(0xFFFFDAD8),
        onPrimaryContainer = BrandDark,
        secondary = Color(0xFF5C6670),
        background = Color(0xFFF8F9FB),
        surface = Color.White,
        surfaceVariant = Color(0xFFF0F2F5),
        onBackground = Color(0xFF1A1D22),
        onSurface = Color(0xFF1A1D22),
        onSurfaceVariant = Color(0xFF5C6670),
        error = Color(0xFFCC0000),
    )

@Composable
fun SampleTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightScheme,
        content = content,
    )
}
