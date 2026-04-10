package com.ae.devlens.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A plugin that provides a visible tab/panel in the AEDevLens UI.
 *
 * Implement this interface to add a visual debugging panel (e.g., logs, network, storage).
 *
 * For headless plugins that only collect data without UI, use [DataPlugin] instead.
 */
interface UIPlugin : DevLensPlugin {
    /** Icon displayed in the tab */
    val icon: ImageVector

    /** The plugin's main UI content, rendered inside DevLens panel */
    @Composable
    fun Content(modifier: Modifier)

    /**
     * Optional flexible slot rendered above the main content.
     * Use for custom controls, toggles, or info banners.
     */
    @Composable
    fun HeaderContent() {}

    /**
     * Optional toolbar action buttons (e.g., Clear, Copy, Export).
     * Rendered in the header row when this plugin's tab is active.
     */
    @Composable
    fun HeaderActions() {}
}
