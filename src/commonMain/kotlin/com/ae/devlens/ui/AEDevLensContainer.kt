package com.ae.devlens.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ae.devlens.core.UIPlugin

/**
 * Adaptive container for the AEDevLens UI.
 *
 * - On phones (compact width): renders as a [ModalBottomSheet]
 * - On tablets/large screens (expanded width): renders as a centered [Dialog]
 *
 * The `isLargeScreen` flag is determined by the caller based on window size metrics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AEDevLensContainer(
    plugins: List<UIPlugin>,
    isLargeScreen: Boolean,
    onDismiss: () -> Unit
) {
    if (isLargeScreen) {
        // Tablets / Desktop → Centered Dialog
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .fillMaxHeight(0.8f),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                DevLensContent(
                    plugins = plugins,
                    onDismiss = onDismiss
                )
            }
        }
    } else {
        // Phones → Bottom Sheet
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            DevLensContent(
                plugins = plugins,
                onDismiss = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.9f)
            )
        }
    }
}
