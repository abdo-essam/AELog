package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.runtime.remember
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
import com.ae.log.ui.components.LogSearchBar
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LogSpacing.x3, vertical = LogSpacing.x2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.popBack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = LogTheme.colors.onSurface,
                )
            }

            Spacer(Modifier.width(LogSpacing.x1))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = db.name,
                    style = LogTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = LogTheme.colors.onSurface,
                )
                Text(
                    text = "${db.engine} • ${DatabaseFormatUtils.formatBytes(db.sizeBytes)}",
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }
        }

        // ── Tabs ──────────────────────────────────────────────────────
        PrimaryTabRow(
            selectedTabIndex = activeTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
        ) {
            TablesTab.entries.forEach { tab ->
                Tab(
                    selected = activeTab == tab,
                    onClick = { viewModel.setTablesTab(tab) },
                    text = {
                        Text(
                            text = tab.label,
                            style = LogTheme.typography.labelMedium,
                            fontWeight = if (activeTab == tab) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

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
                        shape = RoundedCornerShape(LogSpacing.x3),
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
                    TableCardItem(
                        table = table,
                        onClick = { onSelectTable(table) },
                    )
                }
            }

            // ── Database Info Card at bottom ──────────────────────────
            item {
                Spacer(Modifier.height(LogSpacing.x2))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LogSpacing.x3),
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
private fun TableCardItem(
    table: DbTable,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LogSpacing.x3))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        shape = RoundedCornerShape(LogSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(22.dp),
                )
            }

            Spacer(Modifier.width(LogSpacing.x3))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = table.name,
                    style = LogTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LogTheme.colors.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                val rowsText = if (table.rowCount >= 0) "${table.rowCount} rows" else "— rows"
                Text(
                    text = if (table.isSystemTable) "$rowsText • system table" else rowsText,
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LogTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(LogSpacing.x5),
            )
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
                    .clip(RoundedCornerShape(LogSpacing.x3))
                    .clickable { onSelectTable(table) },
                shape = RoundedCornerShape(LogSpacing.x3),
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
