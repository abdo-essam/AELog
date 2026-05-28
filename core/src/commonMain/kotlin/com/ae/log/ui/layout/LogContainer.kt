package com.ae.log.ui.layout

import androidx.compose.runtime.Composable
import com.ae.log.plugin.UIPlugin
import com.ae.log.ui.layout.overlay.BottomSheetOverlay
import com.ae.log.ui.layout.overlay.DialogOverlay
import com.ae.log.ui.layout.overlay.OverlayStrategy

/**
 * Dispatches to the correct [OverlayStrategy] based on screen size.
 *
 * - Compact screens -> bottom sheet
 * - Large screens -> dialog
 */
@Composable
internal fun LogContainer(
    plugins: List<UIPlugin>,
    isLargeScreen: Boolean,
    onDismiss: () -> Unit,
) {
    val strategy: OverlayStrategy = if (isLargeScreen) DialogOverlay else BottomSheetOverlay

    strategy.Overlay(onDismiss = onDismiss) {
        LogContent(
            plugins = plugins,
            onDismiss = onDismiss,
        )
    }
}
