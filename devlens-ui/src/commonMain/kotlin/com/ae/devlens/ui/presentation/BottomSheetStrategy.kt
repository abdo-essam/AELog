package com.ae.devlens.ui.presentation

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Presents the DevLens panel as a [ModalBottomSheet].
 * Suited for compact/phone screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
public object BottomSheetStrategy : PresentationStrategy {
    @Composable
    override fun Present(
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier =
                    androidx.compose.ui.Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
            ) {
                content()
            }
        }
    }
}
