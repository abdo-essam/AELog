package com.ae.log.plugins.network.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.network.model.NetworkEntry

@Composable
internal fun MethodBadge(label: String) {
    val color =
        when (label) {
            "GET" -> Color(0xFF2196F3)
            "POST" -> Color(0xFF4CAF50)
            "PUT" -> Color(0xFFFF9800)
            "PATCH" -> Color(0xFF9C27B0)
            "DELETE" -> Color(0xFFF44336)
            else -> Color(0xFF607D8B)
        }
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
            entry.isSuccess -> Color(0xFF4CAF50)
            entry.statusCode != null && entry.statusCode in 300..399 -> Color(0xFF9C27B0)
            entry.statusCode != null && entry.statusCode in 100..199 -> Color(0xFF2196F3)
            entry.isError -> MaterialTheme.colorScheme.error
            else -> Color(0xFFFFC107)
        }
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
}
