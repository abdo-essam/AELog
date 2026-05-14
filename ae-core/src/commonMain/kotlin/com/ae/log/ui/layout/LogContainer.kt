package com.ae.log.ui.layout

import androidx.compose.runtime.Composable
import com.ae.log.plugin.UIPlugin
import com.ae.log.ui.PresentationMode
import com.ae.log.ui.UiConfig
import com.ae.log.ui.layout.overlay.BottomSheetOverlay
import com.ae.log.ui.layout.overlay.DialogOverlay
import com.ae.log.ui.layout.overlay.OverlayStrategy

/**
 * Dispatches to the correct [OverlayStrategy] based on screen size and [PresentationMode].
 *
 * - [PresentationMode.Adaptive] → bottom sheet on compact, dialog on large screens
 * - [PresentationMode.BottomSheet] → always bottom sheet
 * - [PresentationMode.Dialog] → always dialog
 */
@Composable
internal fun LogContainer(
    plugins: List<UIPlugin>,
    uiConfig: UiConfig,
    isLargeScreen: Boolean,
    presentationMode: PresentationMode,
    onDismiss: () -> Unit,
) {
    val strategy: OverlayStrategy =
        when (presentationMode) {
            PresentationMode.BottomSheet -> BottomSheetOverlay
            PresentationMode.Dialog -> DialogOverlay
            PresentationMode.Adaptive -> if (isLargeScreen) DialogOverlay else BottomSheetOverlay
        }

    strategy.Overlay(uiConfig = uiConfig, onDismiss = onDismiss) {
        LogContent(
            plugins = plugins,
            onDismiss = onDismiss,
        )
    }
}
