@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.analytics.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.analytics.model.AnalyticsEvent
import com.ae.log.plugins.analytics.model.AnalyticsFilters
import com.ae.log.ui.components.LogFilterChips
import com.ae.log.ui.components.LogSearchBar
import com.ae.log.ui.components.PanelHeader
import com.ae.log.ui.theme.LogSpacing
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Composable
internal fun AnalyticsContent(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier,
) {
    val events by viewModel.filteredEvents.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()

    var expandedId by remember { mutableStateOf<String?>(null) }
    val clipboard = LocalClipboardManager.current
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Header ─────────────────────────────────────────────────────────
        PanelHeader(
            itemCount = events.size,
            itemLabel = "events",
            onClearAll = { viewModel.clear() },
        )

        Spacer(Modifier.height(LogSpacing.x3))

        // ── Search bar ─────────────────────────────────────────────────────
        LogSearchBar(
            query = query,
            onQueryChange = { viewModel.search(it) },
            placeholder = "Search event name, property…",
            modifier = Modifier.padding(horizontal = LogSpacing.x5),
        )

        Spacer(Modifier.height(LogSpacing.x3))

        // ── Filter chips ───────────────────────────────────────────────────
        LogFilterChips(
            labels = AnalyticsFilters.defaultFilters.map { it.label },
            selectedIndex = AnalyticsFilters.defaultFilters.indexOf(filter).takeIf { it >= 0 } ?: 0,
            onSelect = { index ->
                val newFilter = AnalyticsFilters.defaultFilters.getOrNull(index) ?: AnalyticsFilters.ALL
                viewModel.setFilter(newFilter)
            },
            modifier = Modifier.padding(horizontal = LogSpacing.x5),
        )

        Spacer(Modifier.height(LogSpacing.x3))

        // ── Content ────────────────────────────────────────────────────────
        if (events.isEmpty()) {
            AnalyticsEmptyPlaceholder(query)
        } else {
            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LogSpacing.x5),
                shape = RoundedCornerShape(LogSpacing.x3),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = LogSpacing.x2),
                ) {
                    itemsIndexed(events, key = { _, e -> e.id }) { index, event ->
                        AnalyticsEventItem(
                            event = event,
                            isExpanded = expandedId == event.id,
                            onToggleExpand = {
                                expandedId = if (expandedId == event.id) null else event.id
                            },
                            onCopy = {
                                clipboard.setText(AnnotatedString(event.toClipboardText()))
                            },
                        )
                        if (index < events.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = LogSpacing.x3),
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

// ── Event item ────────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsEventItem(
    event: AnalyticsEvent,
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
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
    ) {
        // ── Summary row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = event.timestamp.toTimeLabel(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (event.properties.isNotEmpty()) {
                    Text(
                        text = event.properties.entries.joinToString(" · ") { "${it.key}=${it.value}" },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Spacer(Modifier.width(LogSpacing.x2))
            Icon(
                imageVector =
                    if (isExpanded) {
                        Icons.Default.KeyboardArrowUp
                    } else {
                        Icons.Default.KeyboardArrowDown
                    },
                contentDescription = null,
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
            AnalyticsEventDetails(event = event, onCopy = onCopy)
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsEventDetails(
    event: AnalyticsEvent,
    onCopy: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = LogSpacing.x3),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(LogSpacing.x2),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                ),
        ) {
            Column(modifier = Modifier.padding(LogSpacing.x3)) {
                // Event name
                Text(
                    "Event",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    event.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Source
                event.source?.let {
                    Spacer(Modifier.height(LogSpacing.x2))
                    Text(
                        "Source",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        it.sourceName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Timestamp
                Spacer(Modifier.height(LogSpacing.x2))
                Text(
                    "Time",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    event.timestamp.toFullTimeLabel(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                // Properties
                if (event.properties.isNotEmpty()) {
                    Spacer(Modifier.height(LogSpacing.x2))
                    Text(
                        "Properties",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        event.properties.entries.forEach { (k, v) ->
                            SuggestionChip(
                                onClick = {},
                                label = {
                                    Text(
                                        "$k = $v",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                                colors =
                                    SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    ),
                            )
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = LogSpacing.x2),
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

// ── Empty placeholder ─────────────────────────────────────────────────────────

@Composable
private fun AnalyticsEmptyPlaceholder(query: String) {
    Box(Modifier.fillMaxSize().padding(LogSpacing.x8), Alignment.Center) {
        Text(
            text = if (query.isNotEmpty()) "No results for \"$query\"" else "No events recorded yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

// ── Time helpers ──────────────────────────────────────────────────────────────

private fun Long.toTimeLabel(): String {
    val t =
        Instant
            .fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())

    fun Int.pad() = toString().padStart(2, '0')
    return "${t.hour.pad()}:${t.minute.pad()}:${t.second.pad()}"
}

private fun Long.toFullTimeLabel(): String {
    val t =
        kotlin.time.Instant
            .fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())

    fun Int.pad() = toString().padStart(2, '0')
    return "${t.date} ${t.hour.pad()}:${t.minute.pad()}:${t.second.pad()}"
}

// ── Clipboard helper ──────────────────────────────────────────────────────────

private fun AnalyticsEvent.toClipboardText(): String =
    buildString {
        appendLine("Event: $name")
        source?.let { appendLine("Source: ${it.sourceName}") }
        appendLine("Time: ${timestamp.toFullTimeLabel()}")
        if (properties.isNotEmpty()) {
            appendLine("Properties:")
            properties.entries.forEach { (k, v) -> appendLine("  $k = $v") }
        }
    }
