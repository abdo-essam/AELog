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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
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
import com.ae.log.ui.components.LogSearchBar
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    val displayedLogs = if (targetDb != null) {
        logs.filter { it.databaseName == targetDb.name }
    } else {
        logs
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogTheme.colors.background),
    ) {
        // ── Top Bar ───────────────────────────────────────────────────
        if (showBackButton) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LogSpacing.x3, vertical = LogSpacing.x2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.popBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LogTheme.colors.onSurface,
                        )
                    }

                    Spacer(Modifier.width(LogSpacing.x1))

                    Text(
                        text = "Database Logs",
                        style = LogTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = LogTheme.colors.onSurface,
                    )
                }

                IconButton(onClick = { viewModel.clearLogs() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear logs",
                        tint = LogTheme.colors.onSurfaceVariant,
                    )
                }
            }
        }

        // ── Filter Tabs: All | Queries | Writes | Errors ──────────────
        PrimaryTabRow(
            selectedTabIndex = activeFilter.ordinal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            DatabaseLogFilter.entries.forEach { filter ->
                Tab(
                    selected = activeFilter == filter,
                    onClick = { viewModel.setLogFilter(filter) },
                    text = {
                        Text(
                            text = filter.label,
                            style = LogTheme.typography.labelMedium,
                            fontWeight = if (activeFilter == filter) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        // ── Search bar ────────────────────────────────────────────────
        LogSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.setLogSearchQuery(it) },
            placeholder = "Search SQL, tables, errors…",
            modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
        )

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
                if (searchQuery.isBlank()) "No database logs recorded yet."
                else "No matching logs for \"$searchQuery\"",
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
        modifier = Modifier
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
                val (statusBg, statusTint, statusIcon) = when {
                    !entry.isSuccess -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), Icons.Default.Error)
                    entry.operation in listOf("INSERT", "UPDATE", "REPLACE") ->
                        Triple(Color(0xFFFFF3E0), Color(0xFFE65100), Icons.Default.Edit)
                    entry.operation == "DELETE" ->
                        Triple(Color(0xFFFFEBEE), Color(0xFFC62828), Icons.Default.Close)
                    else -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), Icons.Default.Check)
                }

                Box(
                    modifier = Modifier
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
