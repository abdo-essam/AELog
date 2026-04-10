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
import com.ae.devlens.plugins.logs.model.LogFilter
import com.ae.devlens.plugins.logs.store.LogStore
import com.ae.devlens.plugins.logs.ui.EmptyPlaceholder
import com.ae.devlens.plugins.logs.ui.LogEntryItem
import com.ae.devlens.plugins.logs.ui.LogFilterChips
import com.ae.devlens.plugins.logs.ui.LogSearchBar
import com.ae.devlens.plugins.logs.ui.LogUtils
import com.ae.devlens.plugins.logs.ui.LogViewerHeader
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Main logs panel content — used by [com.ae.devlens.plugins.logs.LogsPlugin].
 */
@Composable
internal fun LogsContent(
    logStore: LogStore,
    modifier: Modifier = Modifier,
    onCloseInspector: () -> Unit = {}
) {
    val logs by logStore.logsFlow.collectAsState()
    val searchQuery by logStore.searchQuery.collectAsState()
    val selectedFilter by logStore.selectedFilter.collectAsState()
    var expandedLogIndex by remember { mutableStateOf<Int?>(null) }

    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    val filteredLogs = remember(logs, searchQuery, selectedFilter) {
        logs.filter { log ->
            val matchesSearch = searchQuery.isEmpty() ||
                    log.message.contains(searchQuery, ignoreCase = true) ||
                    log.tag.contains(searchQuery, ignoreCase = true) ||
                    log.url?.contains(searchQuery, ignoreCase = true) == true

            val matchesFilter = when (selectedFilter) {
                LogFilter.ALL -> true
                LogFilter.NETWORK -> log.isNetworkLog
                LogFilter.ANALYTICS -> log.isAnalytics
            }

            matchesSearch && matchesFilter
        }.reversed()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LogViewerHeader(
            logCount = filteredLogs.size,
            totalCount = logs.size,
            onClearAll = {
                logStore.clear()
                onCloseInspector()
            },
            onCopyAll = {
                val allLogs = LogUtils.formatAllLogsForCopy(filteredLogs)
                clipboardManager.setText(AnnotatedString(allLogs))
            }
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        LogSearchBar(
            query = searchQuery,
            onQueryChange = { logStore.updateSearchQuery(it) },
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5)
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        LogFilterChips(
            selectedFilter = selectedFilter,
            onFilterSelected = { logStore.updateSelectedFilter(it) },
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5)
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        if (filteredLogs.isEmpty()) {
            EmptyPlaceholder()
        } else {
            LogsList(
                logs = filteredLogs,
                listState = listState,
                expandedLogIndex = expandedLogIndex,
                onToggleExpand = { index ->
                    expandedLogIndex = if (expandedLogIndex == index) null else index
                },
                onCopyLog = { log ->
                    val copyText = LogUtils.formatLogForCopy(log)
                    clipboardManager.setText(AnnotatedString(copyText))
                }
            )
        }
    }
}

@Composable
private fun LogsList(
    logs: List<LogEntry>,
    listState: LazyListState,
    expandedLogIndex: Int?,
    onToggleExpand: (Int) -> Unit,
    onCopyLog: (LogEntry) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = DevLensSpacing.x5),
        shape = RoundedCornerShape(DevLensSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = DevLensSpacing.x2)
        ) {
            itemsIndexed(
                items = logs,
                key = { index, log -> "${log.timestamp}_$index" }
            ) { index, log ->
                LogEntryItem(
                    log = log,
                    isExpanded = expandedLogIndex == index,
                    onToggleExpand = { onToggleExpand(index) },
                    onCopy = { onCopyLog(log) }
                )

                if (index < logs.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = DevLensSpacing.x3),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}
