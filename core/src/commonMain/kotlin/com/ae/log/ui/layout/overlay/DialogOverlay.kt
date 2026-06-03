package com.ae.log.ui.layout.overlay

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogTheme

/**
 * Presents the AELog panel as a centered [Dialog].
 * Suited for large screens (tablets, desktop).
 */
public object DialogOverlay : OverlayStrategy {
    @Composable
    override fun Overlay(
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
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(LogDimens.overlayCornerRadius),
                color = LogTheme.colors.surface,
                tonalElevation = LogDimens.dialogTonalElevation,
            ) {
                content()
            }
        }
    }
}
