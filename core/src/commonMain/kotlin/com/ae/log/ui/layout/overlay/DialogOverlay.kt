package com.ae.log.ui.layout.overlay

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogTheme
import com.ae.log.ui.theme.LogThemeMode

/**
 * Presents the AELog panel as a centered [Dialog].
 * Suited for large screens (tablets, desktop).
 */
public object DialogOverlay : OverlayStrategy {
    @Composable
    override fun Overlay(
        themeMode: LogThemeMode,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        LogTheme(themeMode = themeMode) {
            Dialog(
                onDismissRequest = onDismiss,
                properties = DialogProperties(usePlatformDefaultWidth = false),
            ) {
                Surface(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.94f)
                            .fillMaxHeight(0.90f),
                    shape = RoundedCornerShape(LogDimens.overlayCornerRadius),
                    color = LogTheme.colors.surface,
                    tonalElevation = 0.dp,
                ) {
                    content()
                }
            }
        }
    }
}
