package com.ae.log.logs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.ae.log.logs.model.LogEntry
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun LogDetailsContent(log: LogEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(LogSpacing.x2)) {
        if (log.message.isNotBlank()) {
            Column {
                Text(
                    text = "Message",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(LogSpacing.x2))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(LogSpacing.x2),
                ) {
                    Text(
                        text = log.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}
