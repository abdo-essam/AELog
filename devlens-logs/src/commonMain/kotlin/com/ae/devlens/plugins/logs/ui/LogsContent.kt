package com.ae.devlens.plugins.logs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.ae.devlens.plugins.logs.model.LogEntry
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Main logs panel content — used by [com.ae.devlens.plugins.logs.LogsPlugin].
 *
 * Reads all state from [LogsViewModel] — no direct dependency on [com.ae.devlens.plugins.logs.store.LogStore].
 */
@Composable
internal fun LogsContent(
    viewModel: LogsViewModel,
    modifier: Modifier = Modifier,
    onCloseInspector: () -> Unit = {},
) {
    val allLogs by viewModel.filteredLogs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var expandedLogId by remember { mutableStateOf<String?>(null) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    // Newest entries at the top
    val displayLogs = remember(allLogs) { allLogs.reversed() }

    Column(modifier = modifier.fillMaxWidth()) {
        LogViewerHeader(
            logCount = displayLogs.size,
            totalCount = allLogs.size,
            onClearAll = { viewModel.clearLogs() },
            onCopyAll = {
                val text = LogUtils.formatAllLogsForCopy(displayLogs)
                clipboardManager.setText(AnnotatedString(text))
            },
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        LogSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5),
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        LogFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = { viewModel.updateSelectedFilter(it) },
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5),
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        if (displayLogs.isEmpty()) {
            EmptyPlaceholder()
        } else {
            LogsList(
                logs = displayLogs,
                listState = listState,
                expandedLogId = expandedLogId,
                onToggleExpand = { id ->
                    expandedLogId = if (expandedLogId == id) null else id
                },
                onCopyLog = { log ->
                    clipboardManager.setText(AnnotatedString(LogUtils.formatLogForCopy(log)))
                },
            )
        }
    }
}

@Composable
private fun LogsList(
    logs: List<LogEntry>,
    listState: LazyListState,
    expandedLogId: String?,
    onToggleExpand: (String) -> Unit,
    onCopyLog: (LogEntry) -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = DevLensSpacing.x5),
        shape = RoundedCornerShape(DevLensSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = DevLensSpacing.x2),
        ) {
            itemsIndexed(
                items = logs,
                key = { _, log -> log.id },
            ) { index, log ->
                LogEntryItem(
                    log = log,
                    isExpanded = expandedLogId == log.id,
                    onToggleExpand = { onToggleExpand(log.id) },
                    onCopy = { onCopyLog(log) },
                )
                if (index < logs.lastIndex) {
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
