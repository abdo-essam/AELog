package com.ae.log.plugins.log.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.ae.log.plugins.log.model.*
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.ui.components.ListPanel

/**
 * Main logs panel content — used by [com.ae.log.plugins.log.LogPlugin].
 *
 * Reads all state from [LogViewModel] — no direct dependency on [com.ae.log.plugins.log.storage.LogStorage].
 */
@Composable
internal fun LogContent(
    viewModel: LogViewModel,
    modifier: Modifier = Modifier,
) {
    val allLogs by viewModel.filteredLogs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var expandedLogId by remember { mutableStateOf<String?>(null) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val onToggleExpand =
        remember {
            { logId: String ->
                expandedLogId = if (expandedLogId == logId) null else logId
            }
        }

    val onCopyLog =
        remember(clipboardManager) {
            { log: LogEntry ->
                clipboardManager.setText(AnnotatedString(LogUtils.formatLogForCopy(log)))
            }
        }

    ListPanel(
        items = allLogs,
        itemLabel = "entries",
        searchQuery = searchQuery,
        searchPlaceholder = "Search tag or message…",
        onSearchChange = { viewModel.updateSearchQuery(it) },
        filterLabels = LogSeverityFilters.defaultFilters.map { it.label },
        selectedFilterIndex = LogSeverityFilters.defaultFilters.indexOf(selectedFilter).takeIf { it >= 0 } ?: 0,
        onFilterSelect = { index ->
            val filter = LogSeverityFilters.defaultFilters.getOrNull(index) ?: LogSeverityFilters.ALL
            viewModel.updateSelectedFilter(filter)
        },
        onClearAll = { viewModel.clearLogs() },
        onCopyAll = {
            val text = LogUtils.formatAllLogsForCopy(allLogs)
            clipboardManager.setText(AnnotatedString(text))
        },
        emptyMessage = "No logs found",
        emptyQueryMessage = "No results for \"$searchQuery\"",
        listState = listState,
        itemKey = { it.id },
        modifier = modifier,
    ) { _, log ->
        LogEntryItem(
            log = log,
            isExpanded = expandedLogId == log.id,
            onToggleExpand = onToggleExpand,
            onCopy = onCopyLog,
        )
    }
}
