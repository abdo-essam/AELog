package com.ae.log.ui

import androidx.compose.runtime.Composable
import com.ae.log.PresentationMode
import com.ae.log.core.UIPlugin
import com.ae.log.ui.presentation.BottomSheetStrategy
import com.ae.log.ui.presentation.DialogStrategy
import com.ae.log.ui.presentation.PresentationStrategy

/**
 * Dispatches to the correct [PresentationStrategy] based on screen size and [PresentationMode].
 *
 * - [PresentationMode.Adaptive] → bottom sheet on compact, dialog on large screens
 * - [PresentationMode.BottomSheet] → always bottom sheet
 * - [PresentationMode.Dialog] → always dialog
 */
@Composable
internal fun LogContainer(
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
        LogContent(
            plugins = plugins,
            onDismiss = onDismiss,
        )
    }
}
