package com.ae.log.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * AELog Material3 theme. Uses the default built-in LightColorScheme.
 */
@Composable
internal fun LogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
