package com.ae.log.plugins.log.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun EmptyPlaceholder() {
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(LogSpacing.x10),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📭",
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(LogSpacing.x3))

            Text(
                text = "No logs found",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(LogSpacing.x1))

            Text(
                text = "Logs will appear here as they are generated",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
