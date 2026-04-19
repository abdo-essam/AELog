package com.ae.devlens.ui.presentation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Presents the DevLens panel as a centered [Dialog].
 * Suited for large screens (tablets, desktop).
 */
public object DialogStrategy : PresentationStrategy {
    @Composable
    override fun Present(
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
                        .fillMaxWidth(0.85f)
                        .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
            ) {
                content()
            }
        }
    }
}
