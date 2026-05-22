package com.ae.log.crashes.ui

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ae.log.crashes.model.CrashEvent
import com.ae.log.crashes.model.CrashFilter
import com.ae.log.ui.components.LogList

/**
 * Main crashes panel — reads all state from [CrashViewModel].
 *
 * No direct dependency on [com.ae.log.crashes.storage.CrashStorage].
 */
@Composable
internal fun CrashContent(
    viewModel: CrashViewModel,
    modifier: Modifier = Modifier,
) {
    val events by viewModel.filteredEvents.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var expandedEventId by remember { mutableStateOf<String?>(null) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val filters = CrashFilter.entries
    val filterLabels = remember { filters.map { it.label } }

    val onToggleExpand =
        remember {
            { id: String -> expandedEventId = if (expandedEventId == id) null else id }
        }

    val onCopyEvent =
        remember(clipboardManager) {
            { event: CrashEvent ->
                clipboardManager.setText(AnnotatedString(CrashUtils.formatEventForCopy(event)))
            }
        }

    LogList(
        items = events,
        itemLabel = "crashes",
        searchQuery = searchQuery,
        searchPlaceholder = "Search exception, message, thread…",
        onSearchChange = { viewModel.updateSearchQuery(it) },
        filterLabels = filterLabels,
        selectedFilterIndex = filters.indexOf(selectedFilter).takeIf { it >= 0 } ?: 0,
        onFilterSelect = { index ->
            viewModel.updateFilter(filters.getOrElse(index) { CrashFilter.ALL })
        },
        onClearAll = { viewModel.clearAll() },
        onCopyAll = {
            clipboardManager.setText(AnnotatedString(CrashUtils.formatAllEventsForCopy(events)))
        },
        emptyMessage = "No crashes recorded",
        emptyQueryMessage = "No results for \"$searchQuery\"",
        listState = listState,
        itemKey = { it.id },
        modifier = modifier,
    ) { _, event ->
        CrashEventItem(
            event = event,
            isExpanded = expandedEventId == event.id,
            onToggleExpand = onToggleExpand,
            onCopy = onCopyEvent,
        )
    }
}
