package com.ae.log.plugins.log.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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
import com.ae.log.plugins.log.model.*
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.ui.components.AELogsExpandedDetails
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun LogEntryItem(
    log: LogEntry,
    isExpanded: Boolean,
    onToggleExpand: (String) -> Unit,
    onCopy: (LogEntry) -> Unit,
) {
    val (_, bgColor) = LogUtils.getLogTypeColor(log)

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = if (isExpanded) "Collapse log entry" else "Expand log entry",
                ) { onToggleExpand(log.id) }
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
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
                onCopy = { onCopy(log) },
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

        Spacer(modifier = Modifier.width(LogSpacing.x3))

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

        Spacer(modifier = Modifier.width(LogSpacing.x2))

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LogEntryPreview(log: LogEntry) {
    val isAnalytics = remember(log.id) { log.isAnalytics }
    val isNetworkLog = remember(log.id) { log.isNetworkLog }

    if (isAnalytics) {
        val titleAndSubtitle =
            remember(log.id) {
                val content = log.message.removePrefix("📊 EVENT: ").removePrefix("📄 PAGE: ")
                val parts = content.split("|", limit = 2)
                Pair(parts.getOrNull(0)?.trim() ?: content, parts.getOrNull(1)?.trim())
            }
        val title = titleAndSubtitle.first
        val subtitle = titleAndSubtitle.second

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
    } else if (isNetworkLog) {
        val method = remember(log.id) { log.httpMethod }
        val status = remember(log.id) { log.httpStatusCode }
        val isRequest = remember(log.id) { log.isRequest }
        val isResponse = remember(log.id) { log.isResponse }

        Row(
            horizontalArrangement = Arrangement.spacedBy(LogSpacing.x2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            method?.let { m ->
                HttpMethodBadge(method = m)
            }

            status?.let { s ->
                HttpStatusBadge(statusCode = s)
            }

            Text(
                text =
                    if (isRequest) {
                        "→"
                    } else if (isResponse) {
                        "←"
                    } else {
                        ""
                    },
                style = MaterialTheme.typography.bodySmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                color = if (isRequest) Color(0xFF2196F3) else Color(0xFF4CAF50),
            )
        }
    } else {
        val cleanMessage = remember(log.id) { log.cleanMessage.take(80).replace("\n", " ") }
        Text(
            text = cleanMessage,
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
    AELogsExpandedDetails(
        bgColor = bgColor,
        onCopy = onCopy,
    ) {
        LogDetailsContent(log = log)
    }
}
