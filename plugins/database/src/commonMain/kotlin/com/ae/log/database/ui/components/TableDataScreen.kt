package com.ae.log.database.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.database.ui.TableDataTab
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.components.LogFilterChips
import com.ae.log.ui.components.LogPaginationBar
import com.ae.log.ui.components.LogScreenHeader
import com.ae.log.ui.components.LogSearchBar
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun TableDataScreen(
    db: DbInfo,
    table: DbTable,
    viewModel: DatabaseViewModel,
    modifier: Modifier = Modifier,
) {
    val activeTab by viewModel.tableDataTab.collectAsState()
    val tableData by viewModel.tableData.collectAsState()
    val tablePage by viewModel.tablePage.collectAsState()
    val pageSize by viewModel.tablePageSize.collectAsState()
    val sortColumn by viewModel.tableSortColumn.collectAsState()
    val sortAscending by viewModel.tableSortAscending.collectAsState()
    val searchQuery by viewModel.tableDataSearchQuery.collectAsState()
    val isTableLoading by viewModel.isTableLoading.collectAsState()

    val tableSchema by viewModel.tableSchema.collectAsState()
    val isSchemaLoading by viewModel.isSchemaLoading.collectAsState()

    val queryEditorSql by viewModel.queryEditorSql.collectAsState()
    val queryEditorResult by viewModel.queryEditorResult.collectAsState()
    val isQueryEditorRunning by viewModel.isQueryEditorRunning.collectAsState()
    val isWriteModeEnabled by viewModel.isWriteModeEnabled.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogTheme.colors.background),
    ) {
        // ── Top Header ────────────────────────────────────────────────
        val rowsMeta = if (table.rowCount >= 0) "${table.rowCount} rows" else "Table"
        val sizeMeta = DatabaseFormatUtils.formatBytes(table.sizeBytes.takeIf { it > 0 } ?: db.sizeBytes)
        LogScreenHeader(
            title = table.name,
            subtitle = "$rowsMeta • $sizeMeta",
            onBackClick = { viewModel.popBack() },
        )

        // ── Filter Chips: Data | Schema | Query ──────────────────────
        LogFilterChips(
            labels = TableDataTab.entries.map { it.label },
            selectedIndex = activeTab.ordinal,
            onSelect = { viewModel.setTableDataTab(TableDataTab.entries[it]) },
            modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
        )

        // ── Tab Content ───────────────────────────────────────────────
        when (activeTab) {
            TableDataTab.DATA -> {
                TableDataTabContent(
                    db = db,
                    table = table,
                    result = tableData,
                    isLoading = isTableLoading,
                    page = tablePage,
                    pageSize = pageSize,
                    sortColumn = sortColumn,
                    sortAscending = sortAscending,
                    searchQuery = searchQuery,
                    onSearchChange = { viewModel.setTableDataSearch(it) },
                    onToggleSort = { viewModel.toggleSort(it) },
                    onNextPage = { viewModel.nextPage() },
                    onPreviousPage = { viewModel.previousPage() },
                    onPageSizeChange = { viewModel.setPageSize(it) },
                    onRowClick = { rowValues, index ->
                        val cols = tableData?.columns ?: emptyList()
                        viewModel.openRowDetails(db, table, cols, rowValues, index)
                    },
                )
            }

            TableDataTab.SCHEMA -> {
                TableSchemaView(
                    table = table,
                    schema = tableSchema,
                    isLoading = isSchemaLoading,
                )
            }

            TableDataTab.QUERY -> {
                QueryEditorView(
                    db = db,
                    viewModel = viewModel,
                    sqlQuery = queryEditorSql,
                    onSqlQueryChange = { viewModel.setQueryEditorSql(it) },
                    queryResult = queryEditorResult,
                    isRunning = isQueryEditorRunning,
                    isWriteModeEnabled = isWriteModeEnabled,
                    onToggleWriteMode = { viewModel.toggleWriteMode() },
                    onRunQuery = { viewModel.runQueryEditor() },
                    tableName = table.name,
                    showBackButton = false,
                )
            }
        }
    }
}

@Composable
private fun TableDataTabContent(
    db: DbInfo,
    table: DbTable,
    result: QueryResult?,
    isLoading: Boolean,
    page: Int,
    pageSize: Int,
    sortColumn: String?,
    sortAscending: Boolean,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleSort: (String) -> Unit,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onPageSizeChange: (Int) -> Unit,
    onRowClick: (List<String?>, Int) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LogSearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search in ${table.name}…",
            modifier = Modifier.padding(horizontal = LogSpacing.x5, vertical = 10.dp),
        )

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(LogSpacing.x8))
                    }
                }

                result == null || (result.columns.isEmpty() && result.rows.isEmpty()) -> {
                    EmptyPlaceholder(
                        if (searchQuery.isNotBlank()) "No records matching \"$searchQuery\""
                        else "No rows found in ${table.name}",
                    )
                }

                else -> {
                    SpreadsheetDataGrid(
                        result = result,
                        sortColumn = sortColumn,
                        sortAscending = sortAscending,
                        onToggleSort = onToggleSort,
                        onRowClick = onRowClick,
                    )
                }
            }
        }

        // ── Pagination Footer: < 1/16 >  Rows per page: 10 ▾ ─────────
        if (result != null && result.columns.isNotEmpty()) {
            LogPaginationBar(
                page = page,
                pageSize = pageSize,
                rowCount = result.rows.size,
                totalRowCount = table.rowCount,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                onPageSizeChange = onPageSizeChange,
                availablePageSizes = listOf(8, 10, 20, 50),
            )
        }
    }
}

@Composable
private fun SpreadsheetDataGrid(
    result: QueryResult,
    sortColumn: String?,
    sortAscending: Boolean,
    onToggleSort: (String) -> Unit,
    onRowClick: (List<String?>, Int) -> Unit,
) {
    val hScroll = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = LogSpacing.x5),
        shape = RoundedCornerShape(LogSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(hScroll),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = LogSpacing.x2),
            ) {
                // Header row with sorting
                item {
                    Row(
                        modifier = Modifier
                            .background(LogTheme.colors.surfaceVariant.copy(alpha = 0.7f))
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        result.columns.forEach { colName ->
                            val isSorted = sortColumn == colName
                            Row(
                                modifier = Modifier
                                    .width(130.dp)
                                    .clickable { onToggleSort(colName) }
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = colName,
                                    style = LogTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = if (isSorted) LogTheme.colors.primary else LogTheme.colors.onSurfaceVariant,
                                    modifier = Modifier.weight(1f, fill = false),
                                )
                                if (isSorted) {
                                    Icon(
                                        imageVector = if (sortAscending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = LogTheme.colors.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = LogTheme.colors.outlineVariant, thickness = LogDimens.listDividerThickness)
                }

                // Data rows
                itemsIndexed(result.rows) { rowIndex, row ->
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                            ) { onRowClick(row, rowIndex) }
                            .padding(vertical = LogSpacing.x3),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        result.columns.indices.forEach { colIndex ->
                            val value = row.getOrNull(colIndex)
                            Text(
                                text = value ?: "null",
                                modifier = Modifier
                                    .width(130.dp)
                                    .padding(horizontal = 10.dp),
                                style = LogTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (value == null)
                                    LogTheme.colors.error.copy(alpha = 0.6f)
                                else
                                    LogTheme.colors.onSurface,
                            )
                        }
                    }

                    if (rowIndex < result.rows.lastIndex) {
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
}

