package com.ae.log.sample.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Brand = Color(0xFF000000)
private val BrandDark = Color(0xFFFFFFFF)
private val BrandLight = Color(0xFFE0E0E0)

private val LightScheme =
    lightColorScheme(
        primary = Brand,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0E0E0),
        onPrimaryContainer = BrandLight,
        secondary = Color(0xFF424242),
        background = Color(0xFFFAFAFA),
        surface = Color.White,
        surfaceVariant = Color(0xFFF5F5F5),
        onBackground = Color(0xFF121212),
        onSurface = Color(0xFF121212),
        onSurfaceVariant = Color(0xFF424242),
        error = Color(0xFFD32F2F),
    )

private val DarkScheme =
    darkColorScheme(
        primary = BrandDark,
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF333333),
        onPrimaryContainer = Color.White,
        secondary = Color(0xFFB0B0B0),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2C2C2C),
        onBackground = Color(0xFFE0E0E0),
        onSurface = Color(0xFFE0E0E0),
        onSurfaceVariant = Color(0xFFB0B0B0),
        error = Color(0xFFD32F2F),
    )

@Composable
fun SampleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkScheme else LightScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content,
    )
}
