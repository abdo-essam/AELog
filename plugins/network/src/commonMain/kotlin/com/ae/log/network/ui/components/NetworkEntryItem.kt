package com.ae.log.network.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.log.network.model.NetworkEntry
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun NetworkEntryItem(
    entry: NetworkEntry,
    isExpanded: Boolean,
    onToggleExpand: (String) -> Unit,
    onCopy: (NetworkEntry) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = if (isExpanded) "Collapse network entry" else "Expand network entry",
                ) { onToggleExpand(entry.id) }
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
    ) {
        // ── Summary row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Method badge
            MethodBadge(entry.rawMethod)
            Spacer(Modifier.width(LogSpacing.x2))

            // URL + timestamp
            Column(modifier = Modifier.weight(1f)) {
                val urlWithoutScheme = entry.url.substringAfter("://", entry.url)
                val pathIndex = urlWithoutScheme.indexOf('/')
                val displayUrl = if (pathIndex != -1) urlWithoutScheme.substring(pathIndex) else urlWithoutScheme

                Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LogSpacing.x2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(entry)
                    entry.durationMs?.let {
                        Text(
                            text = "${it}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text =
                            com.ae.log.utils.TimeUtils
                                .formatTimestamp(entry.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(Modifier.width(LogSpacing.x2))
            Icon(
                imageVector =
                    if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Expanded detail ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            NetworkEntryDetails(entry = entry, onCopy = { onCopy(entry) })
        }
    }
}
