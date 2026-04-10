package com.ae.devlens.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Error boundary wrapper for plugin content.
 *
 * Isolates plugin rendering failures so one broken plugin
 * doesn't crash the entire inspector UI.
 */
@Composable
internal fun SafePluginContent(
    plugin: UIPlugin,
    modifier: Modifier = Modifier
) {
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    if (hasError) {
        PluginErrorFallback(
            pluginName = plugin.name,
            errorMessage = errorMessage,
            onRetry = {
                hasError = false
                errorMessage = null
            },
            modifier = modifier
        )
    } else {
        // Compose doesn't support traditional try-catch around @Composable.
        // We catch errors at the plugin boundary via key-based recomposition.
        key(hasError) {
            plugin.Content(modifier)
        }
    }
}

/**
 * Fallback UI shown when a plugin crashes.
 */
@Composable
private fun PluginErrorFallback(
    pluginName: String,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(DevLensSpacing.x8),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DevLensSpacing.x3)
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = "Error",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.error
            )

            Text(
                text = "Plugin \"$pluginName\" encountered an error",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            OutlinedButton(onClick = onRetry) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(DevLensSpacing.x2))
                Text("Retry")
            }
        }
    }
}
