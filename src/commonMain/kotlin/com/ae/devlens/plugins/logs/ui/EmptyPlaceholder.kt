package com.ae.devlens.plugins.logs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ae.devlens.ui.theme.DevLensSpacing

@Composable
internal fun EmptyPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DevLensSpacing.x10),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "📭",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(DevLensSpacing.x3))

            Text(
                text = "No logs found",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(DevLensSpacing.x1))

            Text(
                text = "Logs will appear here as they are generated",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
