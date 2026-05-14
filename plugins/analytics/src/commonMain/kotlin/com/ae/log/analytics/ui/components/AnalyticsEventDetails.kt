package com.ae.log.analytics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.log.analytics.model.AnalyticsEvent
import com.ae.log.analytics.utils.toFullTimeLabel
import com.ae.log.ui.components.ExpandedDetails
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun AnalyticsEventDetails(
    event: AnalyticsEvent,
    onCopy: () -> Unit,
) {
    ExpandedDetails(
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

        // Properties — replaced FlowRow (requires newer Compose) with simple key=value rows
        if (event.properties.isNotEmpty()) {
            Spacer(Modifier.height(LogSpacing.x2))
            Text(
                "Properties",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            event.properties.entries.forEach { (k, v) ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = k,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(0.4f),
                    )
                    Text(
                        text = "=",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = v.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(0.6f),
                    )
                }
            }
        }
    }
}
