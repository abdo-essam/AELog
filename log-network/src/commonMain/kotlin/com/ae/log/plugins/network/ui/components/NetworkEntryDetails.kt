package com.ae.log.plugins.network.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.plugins.network.model.NetworkEntry
import com.ae.log.plugins.network.utils.extractQueryParams
import com.ae.log.plugins.network.utils.prettyPrintJson
import com.ae.log.ui.components.AELogsExpandedDetails
import com.ae.log.ui.theme.LogSpacing

@Composable
internal fun NetworkEntryDetails(
    entry: NetworkEntry,
    onCopy: () -> Unit,
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Request", "Response")

    val bgColor =
        when {
            entry.isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            entry.isSuccess -> Color(0xFF4CAF50).copy(alpha = 0.07f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }

    val clipboard = LocalClipboardManager.current

    AELogsExpandedDetails(
        bgColor = bgColor,
        onCopy = onCopy,
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }

        Column(modifier = Modifier.padding(LogSpacing.x3)) {
            when (selectedTabIndex) {
                0 -> {
                    // Overview
                    DetailSection("URL", entry.url)
                    entry.statusCode?.let { DetailSection("Status", it.toString()) }
                    entry.durationMs?.let { DetailSection("Duration", "${it}ms") }
                    entry.error?.let { DetailSection("Error", it) }
                }
                1 -> {
                    // Request
                    DetailSection("URL", entry.url)
                    val queryParams = entry.url.extractQueryParams()
                    if (queryParams.isNotEmpty()) {
                        HeadersSection("Query Parameters", queryParams)
                    }
                    if (entry.requestHeaders.isNotEmpty()) {
                        HeadersSection("Headers", entry.requestHeaders)
                    }
                    entry.requestBody?.let {
                        BodySection(
                            label = "Body",
                            body = it.prettyPrintJson(),
                            onCopy = { clipboard.setText(AnnotatedString(it)) },
                        )
                    }
                    if (entry.requestHeaders.isEmpty() && entry.requestBody == null) {
                        Text(
                            "No Request Data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                2 -> {
                    // Response
                    if (entry.responseHeaders.isNotEmpty()) {
                        HeadersSection("Headers", entry.responseHeaders)
                    }
                    entry.responseBody?.let {
                        BodySection(
                            label = "Body",
                            body = it.prettyPrintJson(),
                            onCopy = { clipboard.setText(AnnotatedString(it)) },
                        )
                    }
                    if (entry.responseHeaders.isEmpty() && entry.responseBody == null) {
                        Text(
                            "No Response Data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    value: String,
) {
    Column(modifier = Modifier.padding(bottom = LogSpacing.x2)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun HeadersSection(
    label: String,
    headers: Map<String, String>,
) {
    Column(modifier = Modifier.padding(bottom = LogSpacing.x2).fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
        androidx.compose.foundation.text.selection.SelectionContainer {
            Column(modifier = Modifier.padding(top = 4.dp)) {
                headers.forEach { (key, value) ->
                    Row(modifier = Modifier.padding(bottom = 2.dp)) {
                        Text(
                            text = "$key:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BodySection(
    label: String,
    body: String,
    onCopy: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = LogSpacing.x2).fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            IconButton(
                onClick = { onCopy(body) },
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy $label",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp)
                    .horizontalScroll(rememberScrollState()),
        ) {
            androidx.compose.foundation.text.selection.SelectionContainer {
                Text(
                    text = body,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
