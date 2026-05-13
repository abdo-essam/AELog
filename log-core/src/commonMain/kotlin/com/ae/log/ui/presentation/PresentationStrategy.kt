package com.ae.log.ui.presentation

import androidx.compose.runtime.Composable
import com.ae.log.ui.UiConfig

/**
 * Strategy interface for how the AELog panel is presented on screen.
 *
 * Implement this to create a custom container (e.g., side drawer, floating window).
 * Built-in strategies: [BottomSheetStrategy], [DialogStrategy].
 *
 * ```kotlin
 * val myStrategy = object : PresentationStrategy {
 *     @Composable
 *     override fun Present(content: @Composable () -> Unit, onDismiss: () -> Unit) {
 *         // wrap content however you want
 *     }
 * }
 * ```
 */
public interface PresentationStrategy {
    /**
     * Present the [content] composable using this strategy's container.
     *
     * @param content  The AELog panel content to display.
     * @param onDismiss Called when the user dismisses the panel.
     */
    @Composable
    public fun Present(
        uiConfig: UiConfig,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    )
}
