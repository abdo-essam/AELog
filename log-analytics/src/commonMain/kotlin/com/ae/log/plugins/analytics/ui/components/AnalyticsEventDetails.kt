package com.ae.log.plugins.analytics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.analytics.model.AnalyticsEvent
import com.ae.log.plugins.analytics.utils.toFullTimeLabel
import com.ae.log.ui.components.AELogsExpandedDetails
import com.ae.log.ui.theme.LogSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AnalyticsEventDetails(
    event: AnalyticsEvent,
    onCopy: () -> Unit,
) {
    AELogsExpandedDetails(
        bgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        onCopy = onCopy,
    ) {
        // Event name
        Text(
            "Event",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            event.name,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Source
        event.source?.let {
            Spacer(Modifier.height(LogSpacing.x2))
            Text(
                "Source",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                it.sourceName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Timestamp
        Spacer(Modifier.height(LogSpacing.x2))
        Text(
            "Time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            event.timestamp.toFullTimeLabel(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        // Properties
        if (event.properties.isNotEmpty()) {
            Spacer(Modifier.height(LogSpacing.x2))
            Text(
                "Properties",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                event.properties.entries.forEach { (k, v) ->
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                "$k = $v",
                                style = MaterialTheme.typography.labelSmall,
                            )
                        },
                        colors =
                            SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                    )
                }
            }
        }
    }
}
