package com.ae.log.plugins.network.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.network.model.NetworkEntry
import com.ae.log.ui.theme.NetworkColors

@Composable
internal fun MethodBadge(label: String) {
    val color =
        NetworkColors
            .getMethodColor(label)
    Box(
        modifier =
            Modifier
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
internal fun StatusBadge(entry: NetworkEntry) {
    val text = entry.statusLabel
    val color =
        when {
            entry.isPending -> MaterialTheme.colorScheme.onSurfaceVariant
            entry.isError -> MaterialTheme.colorScheme.error
            else ->
                NetworkColors
                    .getStatusCodeColor(entry.statusCode)
        }
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
}
