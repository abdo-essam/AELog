package com.ae.devlens.ui

import androidx.compose.runtime.Composable
import com.ae.devlens.PresentationMode
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.ui.presentation.BottomSheetStrategy
import com.ae.devlens.ui.presentation.DialogStrategy
import com.ae.devlens.ui.presentation.PresentationStrategy

/**
 * Dispatches to the correct [PresentationStrategy] based on screen size and [PresentationMode].
 *
 * - [PresentationMode.Adaptive] → bottom sheet on compact, dialog on large screens
 * - [PresentationMode.BottomSheet] → always bottom sheet
 * - [PresentationMode.Dialog] → always dialog
 */
@Composable
internal fun AEDevLensContainer(
    plugins: List<UIPlugin>,
    isLargeScreen: Boolean,
    presentationMode: PresentationMode,
    onDismiss: () -> Unit,
) {
    val strategy: PresentationStrategy =
        when (presentationMode) {
            PresentationMode.BottomSheet -> BottomSheetStrategy
            PresentationMode.Dialog -> DialogStrategy
            PresentationMode.Adaptive -> if (isLargeScreen) DialogStrategy else BottomSheetStrategy
        }

    strategy.Present(onDismiss = onDismiss) {
        DevLensContent(
            plugins = plugins,
            onDismiss = onDismiss,
        )
    }
}
