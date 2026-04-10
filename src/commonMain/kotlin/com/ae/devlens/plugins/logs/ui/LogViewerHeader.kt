package com.ae.devlens.plugins.logs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.devlens.ui.theme.DevLensSpacing

@Composable
internal fun LogViewerHeader(
    logCount: Int,
    totalCount: Int,
    onClearAll: () -> Unit,
    onCopyAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevLensSpacing.x5),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Showing $logCount of $totalCount logs",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(DevLensSpacing.x1)) {
            OutlinedButton(
                onClick = onCopyAll,
                contentPadding = PaddingValues(horizontal = DevLensSpacing.x3, vertical = DevLensSpacing.x1)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy all",
                    modifier = Modifier.size(DevLensSpacing.x4)
                )
                Spacer(modifier = Modifier.width(DevLensSpacing.x1))
                Text("Copy", style = MaterialTheme.typography.labelSmall)
            }

            Button(
                onClick = onClearAll,
                contentPadding = PaddingValues(horizontal = DevLensSpacing.x3, vertical = DevLensSpacing.x1)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear all",
                    modifier = Modifier.size(DevLensSpacing.x4)
                )
                Spacer(modifier = Modifier.width(DevLensSpacing.x1))
                Text("Clear", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
