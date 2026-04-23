package com.ae.devlens.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ae.devlens.core.UIPlugin

/**
 * Wrapper for plugin content.
 *
 * Note: Compose does not support try-catch around @Composable functions.
 * Plugin authors must handle their own errors internally to prevent crashes.
 */
@Composable
internal fun PluginContent(
    plugin: UIPlugin,
    modifier: Modifier = Modifier,
) {
    plugin.Content(modifier)
}
