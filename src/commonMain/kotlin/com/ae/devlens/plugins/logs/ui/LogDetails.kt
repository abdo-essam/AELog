package com.ae.devlens.plugins.logs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.devlens.plugins.logs.model.LogEntry
import com.ae.devlens.ui.theme.DevLensSpacing

@Composable
internal fun LogDetailsContent(log: LogEntry) {
    Column(verticalArrangement = Arrangement.spacedBy(DevLensSpacing.x2)) {
        log.url?.let { url ->
            DetailSection(title = "URL", content = url)
        }

        val content = log.jsonBody ?: log.cleanMessage
        if (content.isNotBlank()) {
            DetailSection(
                title = if (log.jsonBody != null) "Body" else "Message",
                content = content,
                isCode = true
            )
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: String,
    isCode: Boolean = false
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(4.dp))

        if (isCode) {
            CodeBlock(content = content)
        } else {
            Text(
                text = content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun CodeBlock(content: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DevLensSpacing.x2))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(DevLensSpacing.x2)
            .horizontalScroll(rememberScrollState())
    ) {
        Text(
            text = content,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
