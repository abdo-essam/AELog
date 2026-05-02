package com.ae.logs.plugins.logs.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.ae.logs.plugins.logs.model.*
import com.ae.logs.plugins.logs.model.LogEntry
import com.ae.logs.ui.components.AELogsFilterChips
import com.ae.logs.ui.components.AELogsSearchBar
import com.ae.logs.ui.components.AELogsViewerHeader
import com.ae.logs.ui.theme.AELogsSpacing

/**
 * Main logs panel content — used by [com.ae.logs.plugins.logs.LogsPlugin].
 *
 * Reads all state from [LogsViewModel] — no direct dependency on [com.ae.logs.plugins.logs.store.LogStore].
 */
@Composable
internal fun LogsContent(
    viewModel: LogsViewModel,
    modifier: Modifier = Modifier,
) {
    val allLogs by viewModel.filteredLogs.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()

    var expandedLogId by remember { mutableStateOf<String?>(null) }

    @Suppress("DEPRECATION")
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    Column(modifier = modifier.fillMaxWidth()) {
        AELogsViewerHeader(
            itemCount = allLogs.size,
            itemLabel = "entries",
            onClearAll = { viewModel.clearLogs() },
            actions = {
                Button(
                    onClick = {
                        val text = LogUtils.formatAllLogsForCopy(allLogs)
                        clipboardManager.setText(AnnotatedString(text))
                    },
                    contentPadding = PaddingValues(horizontal = AELogsSpacing.x3, vertical = AELogsSpacing.x1),
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy all",
                        modifier = Modifier.size(AELogsSpacing.x4),
                    )
                    Spacer(modifier = Modifier.width(AELogsSpacing.x1))
                    Text("Copy All", style = MaterialTheme.typography.labelSmall)
                }
            },
        )

        Spacer(modifier = Modifier.height(AELogsSpacing.x3))

        AELogsSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.padding(horizontal = AELogsSpacing.x5),
        )

        Spacer(modifier = Modifier.height(AELogsSpacing.x3))

        AELogsFilterChips(
            labels = LogFilters.defaultFilters.map { it.label },
            selectedIndex = LogFilters.defaultFilters.indexOf(selectedFilter).takeIf { it >= 0 } ?: 0,
            onSelect = { index ->
                val filter = LogFilters.defaultFilters.getOrNull(index) ?: LogFilters.ALL
                viewModel.updateSelectedFilter(filter)
            },
            modifier = Modifier.padding(horizontal = AELogsSpacing.x5),
        )

        Spacer(modifier = Modifier.height(AELogsSpacing.x3))

        if (allLogs.isEmpty()) {
            EmptyPlaceholder()
        } else {
            LogsList(
                logs = allLogs,
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
                .padding(horizontal = AELogsSpacing.x5),
        shape = RoundedCornerShape(AELogsSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = AELogsSpacing.x2),
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
                        modifier = Modifier.padding(horizontal = AELogsSpacing.x3),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp,
                    )
                }
            }
        }
    }
}
