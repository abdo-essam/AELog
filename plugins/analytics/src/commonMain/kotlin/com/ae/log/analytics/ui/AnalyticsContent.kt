package com.ae.log.analytics.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ae.log.analytics.model.AnalyticsEvent
import com.ae.log.analytics.model.AnalyticsFilters
import com.ae.log.analytics.ui.components.AnalyticsEventItem
import com.ae.log.analytics.utils.toClipboardText
import com.ae.log.ui.components.LogList

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

    val onToggleExpand =
        remember {
            { id: String ->
                expandedId = if (expandedId == id) null else id
            }
        }

    val onCopyEvent =
        remember(clipboard) {
            { event: AnalyticsEvent ->
                clipboard.setText(AnnotatedString(event.toClipboardText()))
            }
        }

    LogList(
        items = events,
        itemLabel = "events",
        searchQuery = query,
        searchPlaceholder = "Search event name, property…",
        onSearchChange = { viewModel.search(it) },
        filterLabels = AnalyticsFilters.defaultFilters.map { it.label },
        selectedFilterIndex = AnalyticsFilters.defaultFilters.indexOf(filter).takeIf { it >= 0 } ?: 0,
        onFilterSelect = { index ->
            val newFilter = AnalyticsFilters.defaultFilters.getOrNull(index) ?: AnalyticsFilters.ALL
            viewModel.setFilter(newFilter)
        },
        onClearAll = { viewModel.clear() },
        onCopyAll = null,
        emptyMessage = "No events recorded yet",
        emptyQueryMessage = "No results for \"$query\"",
        listState = listState,
        itemKey = { it.id },
        modifier = modifier,
    ) { _, event ->
        AnalyticsEventItem(
            event = event,
            isExpanded = expandedId == event.id,
            onToggleExpand = onToggleExpand,
            onCopy = onCopyEvent,
        )
    }
}
