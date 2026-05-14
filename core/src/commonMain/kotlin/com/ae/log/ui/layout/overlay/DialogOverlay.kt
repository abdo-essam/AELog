package com.ae.log.ui.layout.overlay

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ae.log.ui.UiConfig
import com.ae.log.ui.theme.LogDimens

/**
 * Presents the AELog panel as a centered [Dialog].
 * Suited for large screens (tablets, desktop).
 */
public object DialogOverlay : OverlayStrategy {
    @Composable
    override fun Overlay(
        uiConfig: UiConfig,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier =
                    Modifier
                        .fillMaxWidth(uiConfig.dialogSizeFraction.first)
                        .fillMaxHeight(uiConfig.dialogSizeFraction.second),
                shape = RoundedCornerShape(LogDimens.overlayCornerRadius),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = LogDimens.dialogTonalElevation,
            ) {
                content()
            }
        }
    }
}
