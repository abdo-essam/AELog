package com.ae.logs.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.flow.StateFlow

/**
 * A plugin that provides a visible tab/panel in the AELogs UI.
 *
 * Implement this interface to add a visual debugging panel (e.g., logs, network, storage).
 * For headless plugins that only collect data without UI, use [DataPlugin] instead.
 */
public interface UIPlugin : AELogsPlugin {
    /** Icon displayed on the plugin's tab in the AELogs panel. */
    public val icon: ImageVector

    /**
     * Badge count shown on the plugin's tab.
     * A value <= 0 means no badge is shown.
     */
    public val badgeCount: StateFlow<Int>

    /** The plugin's main UI content, rendered inside the AELogs panel body. */
    @Composable
    public fun Content(modifier: Modifier)

    /**
     * Optional flexible slot rendered above the main content.
     * Use for search bars, custom controls, toggles, or info banners.
     */
    @Composable
    public fun HeaderContent() {}

    /**
     * Optional toolbar action buttons (e.g., Clear, Copy, Export).
     * Rendered in the header row when this plugin's tab is active.
     */
    @Composable
    public fun HeaderActions() {}
}
