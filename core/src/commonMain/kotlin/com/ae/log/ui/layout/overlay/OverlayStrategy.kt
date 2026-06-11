package com.ae.log.ui.layout.overlay

import androidx.compose.runtime.Composable

/**
 * Strategy interface for how the AELog panel is overlaid on the screen.
 *
 * Implement this to create a custom container (e.g., side drawer, floating window).
 */
public interface OverlayStrategy {
    /**
     * Overlay the [content] composable using this strategy's container.
     *
     * @param onDismiss Called when the user dismisses the panel.
     * @param content   The AELog panel content to display.
     */
    @Composable
    public fun Overlay(
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    )
}
