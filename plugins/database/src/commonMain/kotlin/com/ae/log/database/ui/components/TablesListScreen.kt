package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.database.ui.TablesTab
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.components.LogFilterChips
import com.ae.log.ui.components.LogItemCard
import com.ae.log.ui.components.LogScreenHeader
import com.ae.log.ui.components.LogSearchBar
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun TablesListScreen(
    db: DbInfo,
    viewModel: DatabaseViewModel,
    modifier: Modifier = Modifier,
) {
    val tables by viewModel.tables.collectAsState()
    val searchQuery by viewModel.tablesSearchQuery.collectAsState()
    val activeTab by viewModel.tablesTab.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogTheme.colors.background),
    ) {
        // ── Header ────────────────────────────────────────────────────
        LogScreenHeader(
            title = db.name,
            subtitle = "${db.engine} • ${DatabaseFormatUtils.formatBytes(db.sizeBytes)}",
            onBackClick = { viewModel.popBack() },
            actions = {
                if (activeTab == TablesTab.LOGS) {
                    IconButton(onClick = { viewModel.clearLogs() }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.DeleteSweep,
                            contentDescription = "Clear logs",
                            tint = LogTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(LogSpacing.x5),
                        )
                    }
                }
            },
        )

        // ── Filter Chips ──────────────────────────────────────────────
        LogFilterChips(
            labels = TablesTab.entries.map { it.label },
            selectedIndex = activeTab.ordinal,
            onSelect = { viewModel.setTablesTab(TablesTab.entries[it]) },
            modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
        )

        // ── Tab Content ───────────────────────────────────────────────
        when (activeTab) {
            TablesTab.TABLES -> {
                TablesTabContent(
                    db = db,
                    tables = tables,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setTablesSearchQuery(it) },
                    onSelectTable = { table ->
                        viewModel.selectTable(db, table, navigate = true)
                    },
                )
            }

            TablesTab.SCHEMA -> {
                DatabaseSchemaOverview(
                    tables = tables,
                    onSelectTable = { table ->
                        viewModel.selectTable(db, table, navigate = true)
                    },
                )
            }

            TablesTab.LOGS -> {
                DatabaseLogsScreen(
                    viewModel = viewModel,
                    targetDb = db,
                    showBackButton = false,
                )
            }
        }
    }
}

@Composable
private fun TablesTabContent(
    db: DbInfo,
    tables: List<DbTable>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectTable: (DbTable) -> Unit,
) {
    val filtered = tables.filter {
        searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LogSearchBar(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = "Search tables…",
            modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x3),
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x1),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (filtered.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(LogDimens.cardCornerRadius),
                        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
                    ) {
                        EmptyPlaceholder(
                            if (searchQuery.isBlank()) "No tables found"
                            else "No match for \"$searchQuery\"",
                        )
                    }
                }
            } else {
                items(filtered, key = { it.name }) { table ->
                    val rowsText = if (table.rowCount >= 0) "${table.rowCount} rows" else "— rows"
                    val metaText = if (table.isSystemTable) "$rowsText • system table" else rowsText
                    LogItemCard(
                        title = table.name,
                        subtitle = metaText,
                        icon = {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        iconContainerColor = Color(0xFFE3F2FD),
                        onClick = { onSelectTable(table) },
                    )
                }
            }

            // ── Database Info Card at bottom ──────────────────────────
            item {
                Spacer(Modifier.height(LogSpacing.x2))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LogDimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(
                        containerColor = LogTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Column(modifier = Modifier.padding(LogSpacing.x4)) {
                        Text(
                            text = "Database Info",
                            style = LogTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = LogTheme.colors.onSurface,
                        )
                        Spacer(Modifier.height(LogSpacing.x2))
                        Text(
                            text = "Path: ${db.path}",
                            style = LogTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = LogTheme.colors.onSurfaceVariant,
                            lineHeight = 16.sp,
                        )
                        Spacer(Modifier.height(LogSpacing.x1))
                        Text(
                            text = "Version: ${db.version.ifBlank { "3.42.0" }}",
                            style = LogTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = LogTheme.colors.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(LogSpacing.x3))
            }
        }
    }
}

@Composable
private fun DatabaseSchemaOverview(
    tables: List<DbTable>,
    onSelectTable: (DbTable) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x3),
        verticalArrangement = Arrangement.spacedBy(LogSpacing.x3),
    ) {
        items(tables, key = { it.name }) { table ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(LogDimens.cardCornerRadius))
                    .clickable { onSelectTable(table) },
                shape = RoundedCornerShape(LogDimens.cardCornerRadius),
                colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
            ) {
                Column(modifier = Modifier.padding(LogSpacing.x4)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = table.name,
                            style = LogTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = LogTheme.colors.onSurface,
                        )
                        Text(
                            text = "${table.columns.size} columns",
                            style = LogTheme.typography.labelSmall,
                            color = LogTheme.colors.primary,
                        )
                    }

                    if (table.columns.isNotEmpty()) {
                        Spacer(Modifier.height(LogSpacing.x2))
                        Text(
                            text = table.columns.joinToString(", "),
                            style = LogTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = LogTheme.colors.onSurfaceVariant,
                            lineHeight = 16.sp,
                        )
                    }
                }
            }
        }
    }
}
