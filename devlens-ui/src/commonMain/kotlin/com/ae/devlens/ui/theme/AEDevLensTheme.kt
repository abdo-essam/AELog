package com.ae.devlens.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Brand Colors ──────────────────────────────────────────────────────────────
private val DevLensPrimary = Color(0xFFBF3547)
private val DevLensPrimaryDark = Color(0xFFD46A78)
private val DevLensError = Color(0xFFE53935)
private val DevLensOnPrimary = Color.White

// ── Light Scheme ──────────────────────────────────────────────────────────────
private val LightColorScheme =
    lightColorScheme(
        primary = DevLensPrimary,
        onPrimary = DevLensOnPrimary,
        primaryContainer = Color(0xFFEDE7F6),
        onPrimaryContainer = Color(0xFF1A0056),
        secondary = Color(0xFF00B894),
        onSecondary = Color.White,
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1A1A2E),
        surfaceVariant = Color(0xFFF5F5F5),
        onSurfaceVariant = Color(0xFF49454F),
        outline = Color(0xFFDDDDDD),
        outlineVariant = Color(0xFFEEEEEE),
        error = DevLensError,
        onError = Color.White,
        background = Color(0xFFF8F9FA),
        onBackground = Color(0xFF1A1A2E),
        inverseSurface = Color(0xFF2D2D3F),
        inverseOnSurface = Color(0xFFF5F5F5),
        scrim = Color(0x52000000),
    )

// ── Dark Scheme ───────────────────────────────────────────────────────────────
private val DarkColorScheme =
    darkColorScheme(
        primary = DevLensPrimaryDark,
        onPrimary = Color(0xFF1A0056),
        primaryContainer = Color(0xFF3700B3),
        onPrimaryContainer = Color(0xFFEDE7F6),
        secondary = Color(0xFF55EFC4),
        onSecondary = Color(0xFF003D2E),
        surface = Color(0xFF1E1E2E),
        onSurface = Color(0xFFE8E8E8),
        surfaceVariant = Color(0xFF2D2D3F),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF444455),
        outlineVariant = Color(0xFF333344),
        error = Color(0xFFFF6B6B),
        onError = Color(0xFF1A0000),
        background = Color(0xFF16161F),
        onBackground = Color(0xFFE8E8E8),
        inverseSurface = Color(0xFFE8E8E8),
        inverseOnSurface = Color(0xFF1E1E2E),
        scrim = Color(0x80000000),
    )

/**
 * DevLens Material3 theme.
 *
 * Supports both light and dark modes.
 * Overridable with a custom [ColorScheme] via [com.ae.devlens.DevLensUiConfig].
 */
@Composable
public fun AEDevLensTheme(
    colorScheme: ColorScheme? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme ?: if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}

/** Common spacing scale used throughout the DevLens UI. */
public object DevLensSpacing {
    public val x1: Dp = 4.dp
    public val x2: Dp = 8.dp
    public val x3: Dp = 12.dp
    public val x4: Dp = 16.dp
    public val x5: Dp = 20.dp
    public val x6: Dp = 24.dp
    public val x8: Dp = 32.dp
    public val x10: Dp = 40.dp
    public val x12: Dp = 48.dp
}
