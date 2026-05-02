@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.ae.logs.plugins.network.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import com.ae.logs.plugins.network.model.NetworkEntry
import com.ae.logs.plugins.network.model.NetworkFilters
import com.ae.logs.ui.components.AELogsFilterChips
import com.ae.logs.ui.components.AELogsSearchBar
import com.ae.logs.ui.components.AELogsViewerHeader
import com.ae.logs.ui.theme.AELogsSpacing
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

@Composable
internal fun NetworkContent(
    viewModel: NetworkViewModel,
    modifier: Modifier = Modifier,
) {
    val entries by viewModel.filteredEntries.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()

    val hasPending by viewModel.hasPending.collectAsState()
    
    LaunchedEffect(hasPending, filter) {
        if (!hasPending && filter == NetworkFilters.PENDING) {
            viewModel.setFilter(NetworkFilters.ALL)
        }
    }

    val activeFilters = if (hasPending) {
        NetworkFilters.defaultFilters
    } else {
        NetworkFilters.defaultFilters.filter { it != NetworkFilters.PENDING }
    }

    var expandedId by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Header ─────────────────────────────────────────────────────────
        AELogsViewerHeader(
            itemCount = entries.size,
            itemLabel = "requests",
            onClearAll = { viewModel.clear() },
        )

        Spacer(Modifier.height(AELogsSpacing.x3))

        // ── Search bar ─────────────────────────────────────────────────────
        AELogsSearchBar(
            query = query,
            onQueryChange = { viewModel.search(it) },
            placeholder = "Search URL, method, status…",
            modifier = Modifier.padding(horizontal = AELogsSpacing.x5),
        )

        Spacer(Modifier.height(AELogsSpacing.x3))

        // ── Filter chips ───────────────────────────────────────────────────
        AELogsFilterChips(
            labels = activeFilters.map { it.label },
            selectedIndex = activeFilters.indexOf(filter).takeIf { it >= 0 } ?: 0,
            onSelect = { index ->
                val newFilter = activeFilters.getOrNull(index) ?: NetworkFilters.ALL
                viewModel.setFilter(newFilter)
            },
            modifier = Modifier.padding(horizontal = AELogsSpacing.x5),
        )

        Spacer(Modifier.height(AELogsSpacing.x3))

        // ── Content ────────────────────────────────────────────────────────
        if (entries.isEmpty()) {
            NetworkEmptyPlaceholder(query)
        } else {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AELogsSpacing.x5),
                shape = RoundedCornerShape(AELogsSpacing.x3),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = AELogsSpacing.x2),
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
                                modifier = Modifier.padding(horizontal = AELogsSpacing.x3),
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
                .padding(horizontal = AELogsSpacing.x4, vertical = AELogsSpacing.x3),
    ) {
        // ── Summary row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Method badge
            MethodBadge(entry.rawMethod)
            Spacer(Modifier.width(AELogsSpacing.x2))

            // URL + timestamp
            Column(modifier = Modifier.weight(1f)) {
                val urlWithoutScheme = entry.url.substringAfter("://", entry.url)
                val pathIndex = urlWithoutScheme.indexOf('/')
                val displayUrl = if (pathIndex != -1) urlWithoutScheme.substring(pathIndex) else urlWithoutScheme

                Text(
                    text = displayUrl,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AELogsSpacing.x2),
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

            Spacer(Modifier.width(AELogsSpacing.x2))
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
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Request", "Response")

    val bgColor =
        when {
            entry.isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            entry.isSuccess -> Color(0xFF4CAF50).copy(alpha = 0.07f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        }

    val clipboard = LocalClipboardManager.current

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = AELogsSpacing.x3),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AELogsSpacing.x2),
            colors = CardDefaults.cardColors(containerColor = bgColor),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                }

                Column(modifier = Modifier.padding(AELogsSpacing.x3)) {
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
                            if (entry.requestHeaders.isNotEmpty()) {
                                HeadersSection("Headers", entry.requestHeaders)
                            }
                            entry.requestBody?.let {
                                BodySection(
                                    label = "Body",
                                    body = it.prettyPrintJson(),
                                    onCopy = { clipboard.setText(AnnotatedString(it)) }
                                )
                            }
                            if (entry.requestHeaders.isEmpty() && entry.requestBody == null) {
                                Text("No Request Data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    onCopy = { clipboard.setText(AnnotatedString(it)) }
                                )
                            }
                            if (entry.responseHeaders.isEmpty() && entry.responseBody == null) {
                                Text("No Response Data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = AELogsSpacing.x2),
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
                    "Copy All",
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
    Column(modifier = Modifier.padding(bottom = AELogsSpacing.x2)) {
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
    Column(modifier = Modifier.padding(bottom = AELogsSpacing.x2).fillMaxWidth()) {
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
    onCopy: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = AELogsSpacing.x2).fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "Copy $label",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(16.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onCopy(body) }
            )
        }
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(8.dp)
                .horizontalScroll(rememberScrollState())
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
            entry.statusCode != null && entry.statusCode in 300..399 -> Color(0xFF9C27B0)
            entry.statusCode != null && entry.statusCode in 100..199 -> Color(0xFF2196F3)
            entry.isError -> MaterialTheme.colorScheme.error
            else -> Color(0xFFFFC107)
        }
    Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
}

private val PRETTY_JSON =
    Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
private fun String.prettyPrintJson(): String =
    runCatching {
        val jsonElement = PRETTY_JSON.parseToJsonElement(this)
        PRETTY_JSON.encodeToString(JsonElement.serializer(), jsonElement)
    }.getOrDefault(this)

// ── Empty placeholder ─────────────────────────────────────────────────────────

@Composable
private fun NetworkEmptyPlaceholder(query: String) {
    Box(Modifier.fillMaxSize().padding(AELogsSpacing.x8), Alignment.Center) {
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
