package com.ae.log.analytics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.log.analytics.model.AnalyticsEvent
import com.ae.log.analytics.utils.toFullTimeLabel
import com.ae.log.ui.components.ExpandedDetails
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun AnalyticsEventDetails(
    event: AnalyticsEvent,
    onCopy: () -> Unit,
) {
    ExpandedDetails(
        bgColor = LogTheme.colors.surfaceVariant.copy(alpha = 0.5f),
        onCopy = onCopy,
    ) {
        // Event name
        Text(
            "Event",
            style = LogTheme.typography.labelSmall,
            color = LogTheme.colors.primary,
        )
        Text(
            event.name,
            style = LogTheme.typography.bodySmall,
            color = LogTheme.colors.onSurface,
        )

        // Source
        event.source?.let {
            Spacer(Modifier.height(LogSpacing.x2))
            Text(
                "Source",
                style = LogTheme.typography.labelSmall,
                color = LogTheme.colors.primary,
            )
            Text(
                it.sourceName,
                style = LogTheme.typography.bodySmall,
                color = LogTheme.colors.onSurface,
            )
        }

        // Timestamp
        Spacer(Modifier.height(LogSpacing.x2))
        Text(
            "Time",
            style = LogTheme.typography.labelSmall,
            color = LogTheme.colors.primary,
        )
        Text(
            event.timestamp.toFullTimeLabel(),
            style = LogTheme.typography.bodySmall,
            color = LogTheme.colors.onSurface,
        )

        // Properties — replaced FlowRow (requires newer Compose) with simple key=value rows
        if (event.properties.isNotEmpty()) {
            Spacer(Modifier.height(LogSpacing.x2))
            Text(
                "Properties",
                style = LogTheme.typography.labelSmall,
                color = LogTheme.colors.primary,
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
                        style = LogTheme.typography.bodySmall,
                        color = LogTheme.colors.primary,
                        modifier = Modifier.weight(0.4f),
                    )
                    Text(
                        text = "=",
                        style = LogTheme.typography.bodySmall,
                        color = LogTheme.colors.onSurfaceVariant,
                    )
                    Text(
                        text = v.toString(),
                        style = LogTheme.typography.bodySmall,
                        color = LogTheme.colors.onSurface,
                        modifier = Modifier.weight(0.6f),
                    )
                }
            }
        }
    }
}
