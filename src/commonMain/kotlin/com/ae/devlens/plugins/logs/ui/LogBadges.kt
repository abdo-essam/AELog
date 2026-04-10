package com.ae.devlens.plugins.logs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.devlens.plugins.logs.model.LogEntry

@Composable
internal fun LogTypeBadge(log: LogEntry) {
    val (color, _) = LogUtils.getLogTypeColor(log)
    val label = LogUtils.getBadgeLabel(log)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
internal fun HttpMethodBadge(method: String) {
    val color = LogUtils.getMethodColor(method)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = method,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White
        )
    }
}

@Composable
internal fun HttpStatusBadge(statusCode: Int) {
    val color = LogUtils.getStatusCodeColor(statusCode)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = statusCode.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
