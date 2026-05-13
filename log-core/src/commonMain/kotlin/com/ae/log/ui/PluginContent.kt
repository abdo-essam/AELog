package com.ae.log.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ae.log.plugin.UIPlugin

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
