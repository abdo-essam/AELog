package com.ae.log.ui.layout.overlay

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ae.log.ui.UiConfig
import com.ae.log.ui.theme.LogSpacing

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
                    androidx.compose.ui.Modifier
                        .fillMaxWidth(uiConfig.dialogSizeFraction.first)
                        .fillMaxHeight(uiConfig.dialogSizeFraction.second),
                shape = RoundedCornerShape(LogSpacing.x6),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
            ) {
                content()
            }
        }
    }
}
