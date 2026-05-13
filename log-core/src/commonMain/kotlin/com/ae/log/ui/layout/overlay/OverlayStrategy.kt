package com.ae.log.ui.layout.overlay

import androidx.compose.runtime.Composable
import com.ae.log.ui.UiConfig

/**
 * Strategy interface for how the AELog panel is overlaid on the screen.
 *
 * Implement this to create a custom container (e.g., side drawer, floating window).
 */
public interface OverlayStrategy {
    /**
     * Overlay the [content] composable using this strategy's container.
     *
     * @param uiConfig  UI configuration.
     * @param onDismiss Called when the user dismisses the panel.
     * @param content   The AELog panel content to display.
     */
    @Composable
    public fun Overlay(
        uiConfig: UiConfig,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    )
}
