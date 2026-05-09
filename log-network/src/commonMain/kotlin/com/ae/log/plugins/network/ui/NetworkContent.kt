
package com.ae.log.plugins.network.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ae.log.plugins.network.model.NetworkEntry
import com.ae.log.plugins.network.model.NetworkFilters
import com.ae.log.plugins.network.ui.components.NetworkEntryItem
import com.ae.log.plugins.network.utils.toClipboardText
import com.ae.log.ui.components.ListPanel

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

    val activeFilters =
        if (hasPending) {
            NetworkFilters.defaultFilters
        } else {
            NetworkFilters.defaultFilters.filter { it != NetworkFilters.PENDING }
        }

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
            { entry: NetworkEntry ->
                clipboard.setText(AnnotatedString(entry.toClipboardText()))
            }
        }

    ListPanel(
        items = entries,
        itemLabel = "requests",
        searchQuery = query,
        searchPlaceholder = "Search URL, method, status…",
        onSearchChange = { viewModel.search(it) },
        filterLabels = activeFilters.map { it.label },
        selectedFilterIndex = activeFilters.indexOf(filter).takeIf { it >= 0 } ?: 0,
        onFilterSelect = { index ->
            val newFilter = activeFilters.getOrNull(index) ?: NetworkFilters.ALL
            viewModel.setFilter(newFilter)
        },
        onClearAll = { viewModel.clear() },
        onCopyAll = null,
        emptyMessage = "No requests recorded yet",
        emptyQueryMessage = "No results for \"$query\"",
        listState = listState,
        itemKey = { it.id },
        modifier = modifier,
    ) { _, entry ->
        NetworkEntryItem(
            entry = entry,
            isExpanded = expandedId == entry.id,
            onToggleExpand = onToggleExpand,
            onCopy = onCopyEvent,
        )
    }
}
