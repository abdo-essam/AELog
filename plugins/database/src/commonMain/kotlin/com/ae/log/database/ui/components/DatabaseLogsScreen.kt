package com.ae.log.database.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import com.ae.log.database.model.QueryResult
import com.ae.log.ui.theme.LogDimens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.database.model.DatabaseLogEntry
import com.ae.log.database.model.DatabaseLogFilter
import com.ae.log.database.model.DbInfo
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.components.ExpandedDetails
import com.ae.log.ui.components.LogFilterChips
import com.ae.log.ui.components.LogKeyValueItem
import com.ae.log.ui.components.LogScreenHeader
import com.ae.log.ui.components.LogSearchBar
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val OP_INSERT = "INSERT"
private const val OP_UPDATE = "UPDATE"
private const val OP_DELETE = "DELETE"
private const val OP_REPLACE = "REPLACE"
private const val OP_CREATE = "CREATE"
private const val OP_DROP = "DROP"
private const val OP_ALTER = "ALTER"

private val EDIT_OPERATIONS = listOf(OP_INSERT, OP_UPDATE, OP_REPLACE, OP_CREATE, OP_ALTER)
private val REMOVE_OPERATIONS = listOf(OP_DELETE, OP_DROP)

@Composable
internal fun DatabaseLogsScreen(
    viewModel: DatabaseViewModel,
    targetDb: DbInfo? = null,
    showBackButton: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val logs by viewModel.filteredLogs.collectAsState()
    val activeFilter by viewModel.logFilter.collectAsState()
    val searchQuery by viewModel.logSearchQuery.collectAsState()
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copyToast by remember { mutableStateOf<String?>(null) }
    var expandedLogId by remember { mutableStateOf<String?>(null) }

    val displayedLogs =
        if (targetDb != null) {
            logs.filter { it.databaseName == targetDb.name }
        } else {
            logs
        }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(LogTheme.colors.background),
    ) {
        // ── Top Bar ───────────────────────────────────────────────────
        if (showBackButton) {
            LogScreenHeader(
                title = "Database Logs",
                onBackClick = { viewModel.popBack() },
                actions = {
                    TextButton(
                        onClick = {
                            val text = DatabaseFormatUtils.formatDatabaseLogsForCopy(displayedLogs)
                            clipboard.setText(AnnotatedString(text))
                            copyToast = "All logs copied to clipboard"
                            scope.launch {
                                delay(2000)
                                copyToast = null
                            }
                        },
                        contentPadding = PaddingValues(horizontal = LogSpacing.x2, vertical = LogSpacing.x1),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy all",
                            modifier = Modifier.size(LogSpacing.x4),
                        )
                        Spacer(modifier = Modifier.width(LogSpacing.x1))
                        Text("Copy All", style = LogTheme.typography.labelSmall)
                    }

                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = LogTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(LogSpacing.x5),
                        )
                    }
                },
            )
        }

        // ── Filter Chips: All | Queries | Writes | Errors ─────────────
        val allLogsForCounts by com.ae.log.database.DatabaseLogRecorder.logs
            .collectAsState()
        val relevantLogs =
            if (targetDb != null) {
                allLogsForCounts.filter { it.databaseName == targetDb.name }
            } else {
                allLogsForCounts
            }

        val chipLabels =
            remember(relevantLogs, activeFilter) {
                DatabaseLogFilter.entries.map { filter ->
                    val count =
                        when (filter) {
                            DatabaseLogFilter.ALL -> relevantLogs.size
                            DatabaseLogFilter.SELECTS -> relevantLogs.count { it.operation == "SELECT" && it.isSuccess }
                            DatabaseLogFilter.INSERTS -> relevantLogs.count { it.operation == "INSERT" && it.isSuccess }
                            DatabaseLogFilter.UPDATES -> relevantLogs.count { it.operation == "UPDATE" && it.isSuccess }
                            DatabaseLogFilter.DELETES -> relevantLogs.count { it.operation == "DELETE" && it.isSuccess }
                            DatabaseLogFilter.SCHEMA ->
                                relevantLogs.count {
                                    it.operation in listOf("CREATE", "DROP", "ALTER", "REPLACE") && it.isSuccess
                                }
                            DatabaseLogFilter.ERRORS -> relevantLogs.count { !it.isSuccess || it.operation == "ERROR" }
                        }
                    "${filter.label} ($count)"
                }
            }

        LogFilterChips(
            labels = chipLabels,
            selectedIndex = activeFilter.ordinal,
            onSelect = { viewModel.setLogFilter(DatabaseLogFilter.entries[it]) },
            modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
        )

        // ── Search bar + Clear action ─────────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LogSearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.setLogSearchQuery(it) },
                placeholder = "Search SQL, tables, errors…",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (copyToast != null) {
            Text(
                text = copyToast!!,
                style = LogTheme.typography.labelSmall,
                color = LogTheme.colors.primary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x1),
            )
        }

        // ── Logs List ─────────────────────────────────────────────────
        if (displayedLogs.isEmpty()) {
            EmptyPlaceholder(
                message =
                    if (searchQuery.isBlank()) {
                        "No database operations recorded yet.\nQueries from tables, searches, query editor, or AELog.database.logQuery() will appear here in real-time."
                    } else {
                        "No matching logs for \"$searchQuery\""
                    },
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(displayedLogs, key = { it.id }) { log ->
                    DatabaseLogItem(
                        entry = log,
                        isExpanded = expandedLogId == log.id,
                        viewModel = viewModel,
                        onToggleExpand = {
                            expandedLogId = if (expandedLogId == log.id) null else log.id
                        },
                        onCopy = {
                            clipboard.setText(AnnotatedString(DatabaseFormatUtils.formatDatabaseLogForCopy(log)))
                            copyToast = "Log entry copied to clipboard"
                            scope.launch {
                                delay(2000)
                                copyToast = null
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DatabaseLogItem(
    entry: DatabaseLogEntry,
    isExpanded: Boolean,
    viewModel: DatabaseViewModel,
    onToggleExpand: () -> Unit,
    onCopy: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LogSpacing.x3))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = if (isExpanded) "Collapse log entry" else "Expand log entry",
                    onClick = onToggleExpand,
                ),
        shape = RoundedCornerShape(LogSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Status indicator + operation badge + table + duration + time + expand icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Status icon circle
                val (statusBg, statusTint, statusIcon) =
                    when {
                        !entry.isSuccess -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Default.Error)
                        entry.operation in EDIT_OPERATIONS ->
                            Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Edit)
                        entry.operation in REMOVE_OPERATIONS ->
                            Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.RemoveCircleOutline)
                        else -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Check)
                    }

                Box(
                    modifier =
                        Modifier
                            .size(20.dp)
                            .background(statusBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusTint,
                        modifier = Modifier.size(12.dp),
                    )
                }

                Spacer(Modifier.width(LogSpacing.x2))

                OperationBadge(operation = entry.operation)

                // Flexible middle section for table name & duration
                Row(
                    modifier = Modifier.weight(1f).padding(horizontal = LogSpacing.x2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (!entry.tableName.isNullOrBlank()) {
                        Text(
                            text = entry.tableName,
                            style = LogTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = LogTheme.colors.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        Spacer(Modifier.width(LogSpacing.x1_5))
                    }

                    Text(
                        text = "${entry.durationMs}ms",
                        style = LogTheme.typography.labelSmall,
                        color = LogTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                    )
                }

                // Fixed right section for timestamp & expand arrow
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = DatabaseFormatUtils.formatTime(entry.timestamp),
                        style = LogTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = LogTheme.colors.onSurfaceVariant,
                        maxLines = 1,
                    )

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
                        tint = LogTheme.colors.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(LogSpacing.x2))

            // SQL Query preview
            Text(
                text = entry.sql,
                style = LogTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = LogTheme.colors.onSurface,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )

            // Error message preview if collapsed & failed
            if (!isExpanded && !entry.isSuccess && !entry.errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(LogSpacing.x1_5))
                Text(
                    text = entry.errorMessage,
                    style = LogTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = LogTheme.colors.error,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp,
                )
            }

            // Expanded detail section
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                ExpandedDetails(
                    bgColor = LogTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    onCopy = onCopy,
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        LogKeyValueItem(key = "Database", value = entry.databaseName)
                        if (!entry.tableName.isNullOrBlank()) {
                            LogKeyValueItem(key = "Table", value = entry.tableName)
                        }
                        LogKeyValueItem(key = "Operation", value = entry.operation)
                        LogKeyValueItem(key = "Duration", value = "${entry.durationMs} ms")
                        if (entry.affectedRows != null) {
                            LogKeyValueItem(key = "Affected Rows", value = "${entry.affectedRows}")
                        }
                        LogKeyValueItem(key = "SQL Query", value = entry.sql)
                        if (!entry.isSuccess && !entry.errorMessage.isNullOrBlank()) {
                            LogKeyValueItem(
                                key = "Error Details",
                                value = entry.errorMessage,
                                isDividerVisible = false,
                            )
                        }

                        // Query Data Result Preview (for SELECT statements)
                        if (entry.isSuccess && (entry.operation == "SELECT" || entry.operation == "PRAGMA")) {
                            QueryDataPreview(
                                databaseName = entry.databaseName,
                                sql = entry.sql,
                                viewModel = viewModel,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QueryDataPreview(
    databaseName: String,
    sql: String,
    viewModel: DatabaseViewModel,
) {
    var queryResult by remember(databaseName, sql) { mutableStateOf<QueryResult?>(null) }
    var isLoading by remember(databaseName, sql) { mutableStateOf(true) }

    LaunchedEffect(databaseName, sql) {
        isLoading = true
        withContext(Dispatchers.Default) {
            val dbInfo = viewModel.databases.value.firstOrNull { it.name == databaseName }
            queryResult =
                if (dbInfo != null) {
                    viewModel.inspector.query(dbInfo, sql, allowWrite = false)
                } else {
                    null
                }
        }
        isLoading = false
    }

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = LogSpacing.x2),
    ) {
        Text(
            text = "Query Result Data",
            style = LogTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = LogTheme.colors.primary,
            modifier = Modifier.padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x1),
        )

        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = 180.dp),
            shape = RoundedCornerShape(LogSpacing.x2),
            colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }

                queryResult == null || (!queryResult!!.isSuccess) -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(LogSpacing.x3),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = queryResult?.errorMessage ?: "Unable to load query data",
                            style = LogTheme.typography.labelSmall,
                            color = LogTheme.colors.error,
                        )
                    }
                }

                queryResult!!.rows.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(LogSpacing.x3),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "0 rows returned",
                            style = LogTheme.typography.labelSmall,
                            color = LogTheme.colors.onSurfaceVariant,
                        )
                    }
                }

                else -> {
                    QueryResultMiniGrid(result = queryResult!!)
                }
            }
        }
    }
}

@Composable
private fun QueryResultMiniGrid(result: QueryResult) {
    val hScroll = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(hScroll),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp),
            contentPadding = PaddingValues(vertical = LogSpacing.x1),
        ) {
            item {
                Row(
                    modifier =
                        Modifier
                            .background(LogTheme.colors.surfaceVariant)
                            .padding(vertical = LogSpacing.x1),
                ) {
                    Text(
                        text = "#",
                        modifier = Modifier.width(36.dp).padding(horizontal = LogSpacing.x1_5),
                        style = LogTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LogTheme.colors.primary,
                    )
                    result.columns.forEach { col ->
                        Text(
                            text = col,
                            modifier = Modifier.width(110.dp).padding(horizontal = LogSpacing.x1_5),
                            style = LogTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = LogTheme.colors.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(color = LogTheme.colors.outlineVariant, thickness = LogDimens.listDividerThickness)
            }

            itemsIndexed(result.rows) { index, row ->
                Row(
                    modifier = Modifier.padding(vertical = LogSpacing.x1),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(36.dp).padding(horizontal = LogSpacing.x1_5),
                        style = LogTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = LogTheme.colors.onSurfaceVariant,
                    )
                    result.columns.indices.forEach { i ->
                        val value = row.getOrNull(i)
                        Text(
                            text = value ?: "null",
                            modifier = Modifier.width(110.dp).padding(horizontal = LogSpacing.x1_5),
                            style = LogTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color =
                                if (value == null) {
                                    LogTheme.colors.error.copy(alpha = 0.7f)
                                } else {
                                    LogTheme.colors.onSurface
                                },
                        )
                    }
                }
                if (index < result.rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = LogSpacing.x2),
                        color = LogTheme.colors.outlineVariant.copy(alpha = 0.4f),
                        thickness = LogDimens.listDividerThickness,
                    )
                }
            }
        }
    }
}
