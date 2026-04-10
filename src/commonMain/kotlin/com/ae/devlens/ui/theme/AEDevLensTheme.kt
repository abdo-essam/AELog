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

// --- Brand Colors ---
private val DevLensPrimary = Color(0xFF6C5CE7)      // Purple accent
private val DevLensPrimaryDark = Color(0xFFA29BFE)   // Lighter purple for dark mode
private val DevLensError = Color(0xFFE53935)
private val DevLensOnPrimary = Color.White

// --- Light Scheme ---
private val LightColorScheme = lightColorScheme(
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

// --- Dark Scheme ---
private val DarkColorScheme = darkColorScheme(
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
 * AEDevLens's built-in Material3 theme.
 *
 * Wraps content in a consistent visual style. Supports both light and dark modes.
 * Can be overridden by providing a custom [ColorScheme] in [com.ae.devlens.core.AEDevLensConfig].
 */
@Composable
fun AEDevLensTheme(
    colorScheme: ColorScheme? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val scheme = colorScheme ?: if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}

/**
 * Common spacing values used throughout the DevLens UI.
 */
object DevLensSpacing {
    val x1: Dp = 4.dp
    val x2: Dp = 8.dp
    val x3: Dp = 12.dp
    val x4: Dp = 16.dp
    val x5: Dp = 20.dp
    val x6: Dp = 24.dp
    val x8: Dp = 32.dp
    val x10: Dp = 40.dp
    val x12: Dp = 48.dp
}
