package com.ae.log.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

public object LogTheme {
    public val colors: LogColors = LogColors
    public val typography: LogTypography = LogTypography
}

public object LogColors {
    public val primary: Color = Color(0xFF000000)
    public val onPrimary: Color = Color.White
    public val primaryContainer: Color = Color(0xFFE0E0E0)
    public val onPrimaryContainer: Color = Color(0xFF000000)
    public val secondary: Color = Color(0xFF424242)
    public val onSecondary: Color = Color.White
    public val surface: Color = Color(0xFFFFFFFF)
    public val onSurface: Color = Color(0xFF121212)
    public val surfaceVariant: Color = Color(0xFFF5F5F5)
    public val onSurfaceVariant: Color = Color(0xFF424242)
    public val outline: Color = Color(0xFFBDBDBD)
    public val outlineVariant: Color = Color(0xFFE0E0E0)
    public val error: Color = Color(0xFFD32F2F)
    public val onError: Color = Color.White
    public val errorContainer: Color = Color(0xFFFFDAD6)
    public val onErrorContainer: Color = Color(0xFF410002)
    public val background: Color = Color(0xFFFAFAFA)
    public val onBackground: Color = Color(0xFF121212)
    public val inverseSurface: Color = Color(0xFF121212)
    public val inverseOnSurface: Color = Color(0xFFF5F5F5)
    public val scrim: Color = Color(0x52000000)
}

public object LogTypography {
    public val bodyLarge: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        )
    public val bodyMedium: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        )
    public val bodySmall: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        )
    public val labelLarge: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        )
    public val labelMedium: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        )
    public val labelSmall: TextStyle =
        TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        )
}

@Composable
internal fun LogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        shapes = Shapes(),
        typography = Typography(),
        content = content,
    )
}
