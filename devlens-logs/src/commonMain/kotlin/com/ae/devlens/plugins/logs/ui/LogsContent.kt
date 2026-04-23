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

import com.ae.devlens.ui.components.DevLensViewerHeader
import com.ae.devlens.ui.components.DevLensSearchBar
import com.ae.devlens.ui.components.DevLensFilterChips
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy

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
        DevLensViewerHeader(
            itemCount = displayLogs.size,
            itemLabel = if (allLogs.size != displayLogs.size) "entries (filtered)" else "entries",
            onClearAll = { viewModel.clearLogs() },
            actions = {
                Button(
                    onClick = {
                        val text = LogUtils.formatAllLogsForCopy(displayLogs)
                        clipboardManager.setText(AnnotatedString(text))
                    },
                    contentPadding = PaddingValues(horizontal = DevLensSpacing.x3, vertical = DevLensSpacing.x1)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy all",
                        modifier = Modifier.size(DevLensSpacing.x4)
                    )
                    Spacer(modifier = Modifier.width(DevLensSpacing.x1))
                    Text("Copy All", style = MaterialTheme.typography.labelSmall)
                }
            }
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        DevLensSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier.padding(horizontal = DevLensSpacing.x5),
        )

        Spacer(modifier = Modifier.height(DevLensSpacing.x3))

        DevLensFilterChips(
            selectedFilter = selectedFilter.label,
            onFilterSelected = { label ->
                val filter = LogFilter.entries.firstOrNull { it.label == label } ?: LogFilter.ALL
                viewModel.updateSelectedFilter(filter)
            },
            options = LogFilter.entries.map { it.label },
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
