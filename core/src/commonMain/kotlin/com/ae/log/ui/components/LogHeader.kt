package com.ae.log.ui.components

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
import com.ae.log.ui.theme.LogSpacing

/**
 * Shared panel header used across all AELog plugin panels.
 *
 * Shows a count label on the left and a Clear button on the right —
 * matching the design of `LogViewerHeader`.
 */
@Composable
public fun LogHeader(
    itemCount: Int,
    itemLabel: String = "entries",
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = LogSpacing.x5),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$itemCount $itemLabel",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LogSpacing.x2),
        ) {
            actions?.invoke(this)

            Button(
                onClick = onClearAll,
                contentPadding =
                    PaddingValues(
                        horizontal = LogSpacing.x3,
                        vertical = LogSpacing.x1,
                    ),
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear all",
                    modifier = Modifier.size(LogSpacing.x4),
                )
                Spacer(modifier = Modifier.width(LogSpacing.x1))
                Text("Clear", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
