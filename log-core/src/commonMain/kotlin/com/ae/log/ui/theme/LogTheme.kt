package com.ae.log.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ── Brand Colors ──────────────────────────────────────────────────────────────
private val AELogPrimary = Color(0xFF000000)
private val AELogError = Color(0xFFD32F2F)
private val AELogOnPrimary = Color.White

// ── Light Scheme ──────────────────────────────────────────────────────────────
private val LightColorScheme =
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

/**
 * AELog Material3 theme.
 *
 * Supports both light and dark modes.
 * Overridable with a custom [ColorScheme] via [com.ae.log.UiConfig].
 */
@Composable
public fun LogTheme(
    colorScheme: ColorScheme? = null,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = colorScheme ?: LightColorScheme,
        content = content,
    )
}

/** Common spacing scale used throughout the AELog UI. */
public object LogSpacing {
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
