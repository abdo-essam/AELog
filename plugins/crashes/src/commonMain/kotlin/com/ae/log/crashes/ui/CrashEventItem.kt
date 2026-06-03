package com.ae.log.crashes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.log.crashes.model.CrashEvent
import com.ae.log.ui.components.ExpandedDetails
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun CrashEventItem(
    event: CrashEvent,
    isExpanded: Boolean,
    onToggleExpand: (String) -> Unit,
    onCopy: (CrashEvent) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = if (isExpanded) "Collapse crash details" else "Expand crash details",
                ) { onToggleExpand(event.id) }
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
    ) {
        CrashEventHeader(event = event, isExpanded = isExpanded)

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            ExpandedDetails(
                bgColor = LogTheme.colors.surfaceVariant,
                onCopy = { onCopy(event) },
            ) {
                CrashStackTraceContent(event = event)
            }
        }
    }
}

@Composable
private fun CrashEventHeader(
    event: CrashEvent,
    isExpanded: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CrashSeverityBadge(isFatal = event.isFatal)

        Spacer(modifier = Modifier.width(LogSpacing.x3))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = event.exceptionType,
                    style = LogTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = LogTheme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = CrashUtils.formatTimestamp(event.timestamp),
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(LogSpacing.x1))

            val preview =
                remember(event.id) {
                    event.message.ifBlank {
                        event.stackTrace
                            .lines()
                            .firstOrNull()
                            ?.trim() ?: ""
                    }
                }
            Text(
                text = preview,
                style = LogTheme.typography.labelSmall,
                color = LogTheme.colors.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(LogSpacing.x1))

            Text(
                text = "Thread: ${event.threadName}",
                style = LogTheme.typography.labelSmall,
                color = LogTheme.colors.onSurfaceVariant,
            )
        }

        Spacer(modifier = Modifier.width(LogSpacing.x2))

        Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            modifier = Modifier.size(LogSpacing.x6),
            tint = LogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
private fun CrashSeverityBadge(isFatal: Boolean) {
    val color = if (isFatal) CrashColors.fatal else CrashColors.nonFatal
    val bg = if (isFatal) CrashColors.fatalContainer else CrashColors.nonFatalContainer
    val label = if (isFatal) "FATAL" else "NON-FATAL"

    Surface(
        shape = RoundedCornerShape(4.dp),
        color = bg,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = LogSpacing.x2, vertical = LogSpacing.x1),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(LogSpacing.x1),
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(LogSpacing.x4),
            )
            Text(
                text = label,
                style = LogTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
            )
        }
    }
}

@Composable
private fun CrashStackTraceContent(event: CrashEvent) {
    Column(verticalArrangement = Arrangement.spacedBy(LogSpacing.x2)) {
        if (event.message.isNotBlank()) {
            Text(
                text = event.message,
                style = LogTheme.typography.bodySmall,
                color = LogTheme.colors.onSurfaceVariant,
            )
            HorizontalDivider(color = LogTheme.colors.outlineVariant)
        }
        Text(
            text = event.stackTrace,
            style = LogTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = LogTheme.colors.onSurfaceVariant,
        )
    }
}
