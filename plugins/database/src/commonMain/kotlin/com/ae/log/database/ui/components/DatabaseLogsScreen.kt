package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ae.log.ui.components.LogFilterChips
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

private val QUERY_OPERATIONS = listOf("SELECT", "PRAGMA", "TRANSACTION", "OTHER")
private val WRITE_OPERATIONS = listOf(OP_INSERT, OP_UPDATE, OP_DELETE, OP_REPLACE, OP_CREATE, OP_DROP, OP_ALTER)
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
            remember(relevantLogs.size, activeFilter) {
                DatabaseLogFilter.entries.map { filter ->
                    val count =
                        when (filter) {
                            DatabaseLogFilter.ALL -> relevantLogs.size
                            DatabaseLogFilter.QUERIES ->
                                relevantLogs.count { it.operation in QUERY_OPERATIONS && it.isSuccess }
                            DatabaseLogFilter.WRITES -> relevantLogs.count { it.operation in WRITE_OPERATIONS }
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
                        onCopy = {
                            clipboard.setText(AnnotatedString(log.sql))
                            copyToast = "SQL copied to clipboard"
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
    onCopy: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(LogSpacing.x3))
                .clickable { onCopy() },
        shape = RoundedCornerShape(LogSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Status indicator + operation badge + table + duration + time
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

                if (!entry.tableName.isNullOrBlank()) {
                    Spacer(Modifier.width(LogSpacing.x2))
                    Text(
                        text = entry.tableName,
                        style = LogTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LogTheme.colors.onSurface,
                    )
                }

                Spacer(Modifier.width(LogSpacing.x2))

                // Duration e.g. "4ms"
                Text(
                    text = "${entry.durationMs}ms",
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )

                Spacer(Modifier.weight(1f))

                // Timestamp e.g. "10:42:21"
                Text(
                    text = DatabaseFormatUtils.formatTime(entry.timestamp),
                    style = LogTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(LogSpacing.x2))

            // SQL Query string
            Text(
                text = entry.sql,
                style = LogTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                color = LogTheme.colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp,
            )

            // Error message if present
            if (!entry.isSuccess && !entry.errorMessage.isNullOrBlank()) {
                Spacer(Modifier.height(LogSpacing.x1_5))
                Text(
                    text = entry.errorMessage,
                    style = LogTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = LogTheme.colors.error,
                    lineHeight = 14.sp,
                )
            }
        }
    }
}
