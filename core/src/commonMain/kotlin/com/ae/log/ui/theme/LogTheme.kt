package com.ae.log.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

public enum class LogThemeMode {
    SYSTEM, LIGHT, DARK
}

public object LogTheme {
    public val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.colorScheme

    public val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.typography
}

private object LogTypography {
    val bodyLarge: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        )
    val bodyMedium: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        )
    val bodySmall: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        )
    val labelLarge: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        )
    val labelMedium: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        )
    val labelSmall: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        )
}

@Composable
public fun LogTheme(
    themeMode: LogThemeMode = LogThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        LogThemeMode.SYSTEM -> isSystemInDarkTheme()
        LogThemeMode.LIGHT -> false
        LogThemeMode.DARK -> true
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = Typography(
        bodyLarge = LogTypography.bodyLarge,
        bodyMedium = LogTypography.bodyMedium,
        bodySmall = LogTypography.bodySmall,
        labelLarge = LogTypography.labelLarge,
        labelMedium = LogTypography.labelMedium,
        labelSmall = LogTypography.labelSmall,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(),
        typography = typography,
        content = content,
    )
}
