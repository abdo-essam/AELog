package com.ae.devlens.plugins.logs.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.devlens.plugins.logs.model.LogEntry
import com.ae.devlens.plugins.logs.model.*
import com.ae.devlens.ui.theme.DevLensSpacing

@Composable
internal fun LogEntryItem(
    log: LogEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit,
) {
    val (_, bgColor) = LogUtils.getLogTypeColor(log)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onToggleExpand() }
                .padding(horizontal = DevLensSpacing.x4, vertical = DevLensSpacing.x3),
    ) {
        LogEntryHeader(log = log, isExpanded = isExpanded)

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            LogEntryExpandedContent(
                log = log,
                bgColor = bgColor,
                onCopy = onCopy,
            )
        }
    }
}

@Composable
private fun LogEntryHeader(
    log: LogEntry,
    isExpanded: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LogTypeBadge(log = log)

        Spacer(modifier = Modifier.width(DevLensSpacing.x3))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = log.endpoint ?: log.tag,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = LogUtils.formatTimestamp(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LogEntryPreview(log = log)
        }

        Spacer(modifier = Modifier.width(DevLensSpacing.x2))

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LogEntryPreview(log: LogEntry) {
    if (log.isAnalytics) {
        val content = log.message.removePrefix("📊 EVENT: ").removePrefix("📄 PAGE: ")
        val parts = content.split("|", limit = 2)
        val title = parts.getOrNull(0)?.trim() ?: content
        val subtitle = parts.getOrNull(1)?.trim()

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    } else if (log.isNetworkLog) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DevLensSpacing.x2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            log.httpMethod?.let { method ->
                HttpMethodBadge(method = method)
            }

            log.httpStatusCode?.let { status ->
                HttpStatusBadge(statusCode = status)
            }

            Text(
                text =
                    if (log.isRequest) {
                        "→"
                    } else if (log.isResponse) {
                        "←"
                    } else {
                        ""
                    },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = if (log.isRequest) Color(0xFF2196F3) else Color(0xFF4CAF50),
            )
        }
    } else {
        Text(
            text = log.cleanMessage.take(80).replace("\n", " "),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun LogEntryExpandedContent(
    log: LogEntry,
    bgColor: Color,
    onCopy: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = DevLensSpacing.x3),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DevLensSpacing.x2),
            colors = CardDefaults.cardColors(containerColor = bgColor),
        ) {
            Column(modifier = Modifier.padding(DevLensSpacing.x3)) {
                LogDetailsContent(log = log)
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = DevLensSpacing.x2),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
