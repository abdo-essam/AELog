package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.database.ui.TableDataTab
import com.ae.log.ui.components.EmptyPlaceholder
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
        modifier =
            modifier
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

        // ── Tabs: Data | Schema | Query ──────────────────────────────
        SegmentedTabRow(
            tabs = TableDataTab.entries.map { it.label },
            selectedIndex = activeTab.ordinal,
            onTabSelected = { viewModel.setTableDataTab(TableDataTab.entries[it]) },
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
                        if (searchQuery.isNotBlank()) {
                            "No records matching \"$searchQuery\""
                        } else {
                            "No rows found in ${table.name}"
                        },
                    )
                }

                else -> {
                    SpreadsheetDataGrid(
                        result = result,
                        page = page,
                        pageSize = pageSize,
                        sortColumn = sortColumn,
                        sortAscending = sortAscending,
                        onToggleSort = onToggleSort,
                        onRowClick = onRowClick,
                    )
                }
            }
        }

        // ── Pagination Footer: Rows per page: 10 ▾ ─────────
        if (result != null && result.columns.isNotEmpty()) {
            LogPaginationBar(
                page = page,
                pageSize = pageSize,
                rowCount = result.rows.size,
                totalRowCount = table.rowCount,
                onPreviousPage = onPreviousPage,
                onNextPage = onNextPage,
                onPageSizeChange = onPageSizeChange,
                availablePageSizes = listOf(10, 20, 50, 100, 200),
            )
        }
    }
}

@Composable
private fun SpreadsheetDataGrid(
    result: QueryResult,
    page: Int,
    pageSize: Int,
    sortColumn: String?,
    sortAscending: Boolean,
    onToggleSort: (String) -> Unit,
    onRowClick: (List<String?>, Int) -> Unit,
) {
    val hScroll = rememberScrollState()
    val isScrollable = hScroll.maxValue > 0

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = LogSpacing.x5),
    ) {
        // ── Scroll Indicator & Metadata Bar ──────────────────────────────
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = LogSpacing.x2),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${result.columns.size} columns • ${result.rows.size} rows",
                style = LogTheme.typography.labelSmall,
                color = LogTheme.colors.onSurfaceVariant,
            )

            if (isScrollable) {
                Row(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(LogTheme.colors.primaryContainer.copy(alpha = 0.6f))
                            .padding(horizontal = LogSpacing.x2, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Scroll horizontally",
                        tint = LogTheme.colors.onPrimaryContainer,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Scrollable",
                        style = LogTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LogTheme.colors.onPrimaryContainer,
                    )
                }
            }
        }

        // ── Main Spreadsheet Card ─────────────────────────────────────────
        val listState = rememberLazyListState()

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(LogSpacing.x3),
            colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .drawVerticalScrollbar(listState, color = LogTheme.colors.primary.copy(alpha = 0.5f)),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .horizontalScroll(hScroll),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = LogSpacing.x2),
                    ) {
                        // Header row with sorting
                        item {
                            Row(
                                modifier =
                                    Modifier
                                        .background(LogTheme.colors.surfaceVariant.copy(alpha = 0.7f))
                                        .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "#",
                                    modifier =
                                        Modifier
                                            .width(44.dp)
                                            .padding(horizontal = 10.dp),
                                    style = LogTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = LogTheme.colors.primary,
                                )
                                result.columns.forEach { colName ->
                                    val isSorted = sortColumn == colName
                                    Row(
                                        modifier =
                                            Modifier
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
                                            color =
                                                if (isSorted) {
                                                    LogTheme.colors.primary
                                                } else {
                                                    LogTheme.colors.onSurfaceVariant
                                                },
                                            modifier = Modifier.weight(1f, fill = false),
                                        )
                                        if (isSorted) {
                                            Icon(
                                                imageVector =
                                                    if (sortAscending) {
                                                        Icons.Default.ArrowDropUp
                                                    } else {
                                                        Icons.Default.ArrowDropDown
                                                    },
                                                contentDescription = null,
                                                tint = LogTheme.colors.primary,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = LogTheme.colors.outlineVariant,
                                thickness = LogDimens.listDividerThickness,
                            )
                        }

                        // Data rows
                        itemsIndexed(result.rows) { rowIndex, row ->
                            Row(
                                modifier =
                                    Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                        ) { onRowClick(row, rowIndex) }
                                        .padding(vertical = LogSpacing.x3),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                val recordNumber = (page * pageSize) + rowIndex + 1
                                Text(
                                    text = "$recordNumber",
                                    modifier =
                                        Modifier
                                            .width(44.dp)
                                            .padding(horizontal = 10.dp),
                                    style = LogTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium,
                                    color = LogTheme.colors.onSurfaceVariant,
                                )
                                result.columns.indices.forEach { colIndex ->
                                    val value = row.getOrNull(colIndex)
                                    Text(
                                        text = value ?: "null",
                                        modifier =
                                            Modifier
                                                .width(130.dp)
                                                .padding(horizontal = 10.dp),
                                        style = LogTheme.typography.bodySmall,
                                        fontFamily = FontFamily.Monospace,
                                        fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color =
                                            if (value == null) {
                                                LogTheme.colors.error.copy(alpha = 0.6f)
                                            } else {
                                                LogTheme.colors.onSurface
                                            },
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

                // ── Horizontal Scroll Progress Track ─────────────────────
                if (isScrollable) {
                    val scrollRatio =
                        if (hScroll.maxValue >
                            0
                        ) {
                            (hScroll.value.toFloat() / hScroll.maxValue.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                    BoxWithConstraints(
                        modifier =
                            Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(LogTheme.colors.outlineVariant.copy(alpha = 0.3f)),
                    ) {
                        val totalTrackWidth = maxWidth
                        val thumbWidth = totalTrackWidth * 0.25f
                        val maxThumbOffset = totalTrackWidth - thumbWidth
                        val currentThumbOffset = maxThumbOffset * scrollRatio

                        Box(
                            modifier =
                                Modifier
                                    .fillMaxHeight()
                                    .width(thumbWidth)
                                    .offset(x = currentThumbOffset)
                                    .background(LogTheme.colors.primary, RoundedCornerShape(2.dp)),
                        )
                    }
                }
            }
        }
    }
}

private fun Modifier.drawVerticalScrollbar(
    state: LazyListState,
    color: Color,
): Modifier =
    this.drawWithContent {
        drawContent()

        val visibleItemsInfo = state.layoutInfo.visibleItemsInfo
        val totalItems = state.layoutInfo.totalItemsCount

        if (visibleItemsInfo.isNotEmpty() && totalItems > 0) {
            val firstVisibleItem = visibleItemsInfo.first()
            val visibleItemsCount = visibleItemsInfo.size
            val viewportHeight = size.height

            val thumbHeight =
                (viewportHeight * (visibleItemsCount.toFloat() / totalItems.toFloat())).coerceAtLeast(28.dp.toPx())
            val maxScrollableItems = (totalItems.toFloat() - visibleItemsCount.toFloat()).coerceAtLeast(1f)
            val scrollFraction = (firstVisibleItem.index.toFloat() / maxScrollableItems).coerceIn(0f, 1f)
            val thumbOffsetY =
                ((viewportHeight - thumbHeight) * scrollFraction).coerceIn(
                    0f,
                    viewportHeight - thumbHeight,
                )

            val trackX = size.width - 5.dp.toPx()
            val thumbWidth = 3.5.dp.toPx()

            drawRoundRect(
                color = color,
                topLeft = Offset(trackX, thumbOffsetY),
                size = Size(thumbWidth, thumbHeight),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            )
        }
    }
