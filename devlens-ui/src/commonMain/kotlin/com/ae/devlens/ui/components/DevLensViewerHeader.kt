package com.ae.devlens.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Shared panel header used across all DevLens plugin panels.
 *
 * Shows a count label on the left and a Clear button on the right —
 * matching the design of `LogViewerHeader`.
 */
@Composable
public fun DevLensViewerHeader(
    itemCount: Int,
    itemLabel: String = "entries",
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = DevLensSpacing.x5),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$itemCount $itemLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
            onClick = onClearAll,
            contentPadding =
                PaddingValues(
                    horizontal = DevLensSpacing.x3,
                    vertical = DevLensSpacing.x1,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Clear all",
                modifier = Modifier.size(DevLensSpacing.x4),
            )
            Spacer(modifier = Modifier.width(DevLensSpacing.x1))
            Text("Clear", style = MaterialTheme.typography.labelSmall)
        }
    }
}
