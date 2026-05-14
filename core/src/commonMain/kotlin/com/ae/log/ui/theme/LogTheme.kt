package com.ae.log.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * AELog Material3 theme.
 *
 * Light theme is used by default. Provide a custom [ColorScheme]
 * via [com.ae.log.ui.UiConfig.colorScheme] to override.
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
