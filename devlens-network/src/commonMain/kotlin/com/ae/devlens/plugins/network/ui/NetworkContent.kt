package com.ae.devlens.plugins.network.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.devlens.plugins.network.model.NetworkEntry
import com.ae.devlens.plugins.network.model.NetworkFilter
import com.ae.devlens.ui.components.DevLensFilterChips
import com.ae.devlens.ui.components.DevLensSearchBar
import com.ae.devlens.ui.components.DevLensViewerHeader
import com.ae.devlens.ui.theme.DevLensSpacing

// ── Filter labels list (order must match NetworkFilter.entries) ───────────────
private val FILTER_LABELS = NetworkFilter.entries.map { it.label }

@Composable
internal fun NetworkContent(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier,
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()

    var expandedId by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Header ─────────────────────────────────────────────────────────
        DevLensViewerHeader(
            itemCount = entries.size,
            itemLabel = "requests",
            onClearAll = { viewModel.clear() },
        )

        Spacer(Modifier.height(DevLensSpacing.x3))

        // ── Search bar ─────────────────────────────────────────────────────
        DevLensSearchBar(
            query = query,
            onQueryChange = { viewModel.search(it) },
            placeholder = "Search URL, method, status…",
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5),
        )

        Spacer(Modifier.height(DevLensSpacing.x3))

        // ── Filter chips ───────────────────────────────────────────────────
        DevLensFilterChips(
            labels = FILTER_LABELS,
            selectedIndex = NetworkFilter.entries.indexOf(filter),
            onSelect = { viewModel.setFilter(NetworkFilter.entries[it]) },
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5),
        )

        Spacer(Modifier.height(DevLensSpacing.x3))

        // ── Content ────────────────────────────────────────────────────────
        if (entries.isEmpty()) {
            NetworkEmptyPlaceholder(query)
        } else {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DevLensSpacing.x5),
                shape = RoundedCornerShape(DevLensSpacing.x3),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = DevLensSpacing.x2),
                ) {
                    itemsIndexed(entries, key = { _, e -> e.id }) { index, entry ->
                        NetworkEntryItem(
                            entry = entry,
                            isExpanded = expandedId == entry.id,
                            onToggleExpand = {
                                expandedId = if (expandedId == entry.id) null else entry.id
                            },
                            onCopy = {
                                clipboard.setText(AnnotatedString(entry.toClipboardText()))
                            },
                        )
                        if (index < entries.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = DevLensSpacing.x3),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Entry item ────────────────────────────────────────────────────────────────

@Composable
private fun NetworkEntryItem(
    entry: NetworkEntry,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { onToggleExpand() }
                .padding(horizontal = DevLensSpacing.x4, vertical = DevLensSpacing.x3),
    ) {
        // ── Summary row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Method badge
            MethodBadge(entry.method.label)
            Spacer(Modifier.width(DevLensSpacing.x2))

            // URL + timestamp
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.url,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DevLensSpacing.x2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StatusBadge(entry)
                    entry.durationMs?.let {
                        Text(
                            text = "${it}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Spacer(Modifier.width(DevLensSpacing.x2))
            Icon(
                imageVector =
                    if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // ── Expanded detail ───────────────────────────────────────────────
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            NetworkEntryDetails(entry = entry, onCopy = onCopy)
        }
    }
}

@Composable
private fun NetworkEntryDetails(
    entry: NetworkEntry,
    onCopy: () -> Unit,
) {
    val bgColor =
        when {
            entry.isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            entry.isSuccess -> Color(0xFF4CAF50).copy(alpha = 0.07f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = DevLensSpacing.x3),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(DevLensSpacing.x2),
            colors = CardDefaults.cardColors(containerColor = bgColor),
        ) {
            Column(modifier = Modifier.padding(DevLensSpacing.x3)) {
                // URL full
                DetailSection("URL", entry.url)
                // Request headers
                if (entry.requestHeaders.isNotEmpty()) {
                    DetailSection(
                        "Request Headers",
                        entry.requestHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" },
                    )
                }
                // Request body
                entry.requestBody?.let { DetailSection("Request Body", it) }
                // Status
                entry.statusCode?.let { DetailSection("Status", it.toString()) }
                // Response headers
                if (entry.responseHeaders.isNotEmpty()) {
                    DetailSection(
                        "Response Headers",
                        entry.responseHeaders.entries.joinToString("\n") { "${it.key}: ${it.value}" },
                    )
                }
                // Response body
                entry.responseBody?.let { DetailSection("Response Body", it) }
                // Error
                entry.error?.let { DetailSection("Error", it) }
                // Duration
                entry.durationMs?.let { DetailSection("Duration", "${it}ms") }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = DevLensSpacing.x2),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Copy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun DetailSection(
    label: String,
    value: String,
) {
    Column(modifier = Modifier.padding(bottom = DevLensSpacing.x2)) {
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

// ── Badges ────────────────────────────────────────────────────────────────────

@Composable
private fun MethodBadge(label: String) {
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
private fun StatusBadge(entry: NetworkEntry) {
    val text = entry.statusLabel
    val color =
        when {
            entry.isPending -> MaterialTheme.colorScheme.onSurfaceVariant
            entry.isSuccess -> Color(0xFF4CAF50)
            entry.isError -> MaterialTheme.colorScheme.error
            else -> Color(0xFFFFC107)
        }
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
}

// ── Empty placeholder ─────────────────────────────────────────────────────────

@Composable
private fun NetworkEmptyPlaceholder(query: String) {
    Box(Modifier.fillMaxSize().padding(DevLensSpacing.x8), Alignment.Center) {
        Text(
            text = if (query.isNotEmpty()) "No results for \"$query\"" else "No requests recorded yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Clipboard helper ──────────────────────────────────────────────────────────

private fun NetworkEntry.toClipboardText(): String =
    buildString {
        appendLine("${method.label} $url")
        statusCode?.let { appendLine("Status: $it") }
        durationMs?.let { appendLine("Duration: ${it}ms") }
        requestBody?.let { appendLine("\n--- Request Body ---\n$it") }
        responseBody?.let { appendLine("\n--- Response Body ---\n$it") }
        error?.let { appendLine("\n--- Error ---\n$it") }
    }
