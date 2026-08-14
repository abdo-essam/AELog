package com.ae.log.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// ── Brand Colors ──────────────────────────────────────────────────────────────
internal val AELogPrimary = Color(0xFF000000)
internal val AELogPrimaryDark = Color(0xFFFFFFFF)
internal val AELogError = Color(0xFFD32F2F)
internal val AELogOnPrimary = Color.White
internal val AELogOnPrimaryDark = Color.Black

// ── Light Scheme ──────────────────────────────────────────────────────────────
internal val LightColorScheme =
    lightColorScheme(
        primary = AELogPrimary,
        onPrimary = AELogOnPrimary,
        primaryContainer = Color(0xFFE0E0E0),
        onPrimaryContainer = Color(0xFF000000),
        secondary = Color(0xFF424242),
        onSecondary = Color.White,
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF121212),
        surfaceVariant = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFF424242),
        outline = Color(0xFFBDBDBD),
        outlineVariant = Color(0xFFE0E0E0),
        error = AELogError,
        onError = Color.White,
        background = Color(0xFFFAFAFA),
        onBackground = Color(0xFF121212),
        inverseSurface = Color(0xFF121212),
        inverseOnSurface = Color(0xFFF5F5F5),
        scrim = Color(0x52000000),
    )

// ── Dark Scheme ───────────────────────────────────────────────────────────────
internal val DarkColorScheme =
    darkColorScheme(
        primary = AELogPrimaryDark,
        onPrimary = AELogOnPrimaryDark,
        primaryContainer = Color(0xFF333333),
        onPrimaryContainer = Color(0xFFFFFFFF),
        secondary = Color(0xFFB0B0B0),
        onSecondary = Color.Black,
        surface = Color(0xFF1E1E1E),
        onSurface = Color(0xFFE0E0E0),
        surfaceVariant = Color(0xFF2C2C2C),
        onSurfaceVariant = Color(0xFFB0B0B0),
        outline = Color(0xFF666666),
        outlineVariant = Color(0xFF444444),
        error = AELogError,
        onError = Color.White,
        background = Color(0xFF121212),
        onBackground = Color(0xFFE0E0E0),
        inverseSurface = Color(0xFFF5F5F5),
        inverseOnSurface = Color(0xFF121212),
        scrim = Color(0x7F000000),
    )
