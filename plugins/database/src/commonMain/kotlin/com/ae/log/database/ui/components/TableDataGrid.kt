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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.ae.log.database.model.QueryResult
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/** Switch to card layout when there are more columns than this */
private const val CARD_THRESHOLD = 4

@Composable
internal fun TableDataGrid(
    result: QueryResult?,
    isLoading: Boolean,
    page: Int,
    onNextPage: () -> Unit,
    onPreviousPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(LogSpacing.x8))
            }
        }

        result == null || (result.columns.isEmpty() && result.rows.isEmpty()) -> {
            EmptyPlaceholder("Select a table to view its data")
        }

        else -> {
            Column(modifier = modifier.fillMaxSize()) {
                // Data area
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (result.columns.size > CARD_THRESHOLD) {
                        CardRowLayout(result = result, page = page)
                    } else {
                        CompactTableLayout(result = result, page = page)
                    }
                }

                // Pagination — same style as LogHeader (labelSmall + TextButton)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${result.rows.size} rows · page ${page + 1}",
                        style = LogTheme.typography.labelSmall,
                        color = LogTheme.colors.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onPreviousPage, enabled = page > 0) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous page",
                                modifier = Modifier.size(LogSpacing.x5),
                                tint = if (page > 0) LogTheme.colors.primary
                                else LogTheme.colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${page + 1}",
                            style = LogTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = LogSpacing.x2),
                        )
                        IconButton(onClick = onNextPage, enabled = result.rows.size >= 50) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next page",
                                modifier = Modifier.size(LogSpacing.x5),
                                tint = if (result.rows.size >= 50) LogTheme.colors.primary
                                else LogTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Compact horizontal table (≤ 4 columns) ──────────────────────────────────

@Composable
private fun CompactTableLayout(result: QueryResult, page: Int) {
    val hScroll = rememberScrollState()

    Card(
        modifier = Modifier
            .fillMaxWidth()
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
                contentPadding = PaddingValues(vertical = LogSpacing.x2),
            ) {
                // Header row
                item {
                    Row(
                        modifier = Modifier
                            .background(LogTheme.colors.surfaceVariant)
                            .padding(vertical = LogSpacing.x2),
                    ) {
                        Text(
                            text = "#",
                            modifier = Modifier
                                .width(LogSpacing.x10)
                                .padding(horizontal = LogSpacing.x2),
                            style = LogTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = LogTheme.colors.primary,
                        )
                        result.columns.forEach { col ->
                            Text(
                                text = col,
                                modifier = Modifier
                                    .width(LogSpacing.x12 * 2)
                                    .padding(horizontal = LogSpacing.x2),
                                style = LogTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = LogTheme.colors.onSurfaceVariant,
                            )
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
                        modifier = Modifier.padding(vertical = LogSpacing.x2),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val absoluteRow = (page * 50) + rowIndex + 1
                        Text(
                            text = "$absoluteRow",
                            modifier = Modifier
                                .width(LogSpacing.x10)
                                .padding(horizontal = LogSpacing.x2),
                            style = LogTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            color = LogTheme.colors.onSurfaceVariant,
                        )
                        result.columns.indices.forEach { colIndex ->
                            val value = row.getOrNull(colIndex)
                            Text(
                                text = value ?: "null",
                                modifier = Modifier
                                    .width(LogSpacing.x12 * 2)
                                    .padding(horizontal = LogSpacing.x2),
                                style = LogTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = if (value == null)
                                    LogTheme.colors.error.copy(alpha = 0.7f)
                                else
                                    LogTheme.colors.onSurface,
                            )
                        }
                    }

                    if (rowIndex < result.rows.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = LogSpacing.x3),
                            color = LogTheme.colors.outlineVariant,
                            thickness = LogDimens.listDividerThickness,
                        )
                    }
                }
            }
        }
    }
}

// ─── Card-per-row layout (> 4 columns) ────────────────────────────────────────

@Composable
private fun CardRowLayout(result: QueryResult, page: Int) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            horizontal = LogSpacing.x5,
            vertical = LogSpacing.x2,
        ),
        verticalArrangement = Arrangement.spacedBy(LogSpacing.x2),
    ) {
        itemsIndexed(result.rows) { rowIndex, row ->
            val absoluteRow = (page * 50) + rowIndex + 1
            RowCard(
                rowNumber = absoluteRow,
                columns = result.columns,
                values = row,
            )
        }
    }
}

@Composable
private fun RowCard(
    rowNumber: Int,
    columns: List<String>,
    values: List<String?>,
) {
    var isExpanded by remember { mutableStateOf(true) }

    // Same Card shape as LogList / ExpandedDetails
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LogSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Column {
            // Row header — click to collapse (same pattern as LogEntryItem)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClickLabel = if (isExpanded) "Collapse row" else "Expand row",
                    ) { isExpanded = !isExpanded }
                    .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Row $rowNumber",
                    style = LogTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LogTheme.colors.primary,
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp
                    else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(LogSpacing.x6),
                    tint = LogTheme.colors.onSurfaceVariant,
                )
            }

            // Expanded content — same ExpandedDetails-style inner card
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LogTheme.colors.surfaceVariant)
                        .padding(LogSpacing.x3),
                ) {
                    columns.forEachIndexed { i, colName ->
                        val value = values.getOrNull(i)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = LogSpacing.x2),
                            verticalAlignment = Alignment.Top,
                        ) {
                            // Column label — same weight/style as LogEntryItem tag
                            Text(
                                text = colName,
                                style = LogTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = LogTheme.colors.onSurfaceVariant,
                                modifier = Modifier.width(LogSpacing.x12),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )

                            Spacer(Modifier.width(LogSpacing.x3))

                            // Value
                            Text(
                                text = value ?: "null",
                                style = LogTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                                color = if (value == null)
                                    LogTheme.colors.error.copy(alpha = 0.7f)
                                else
                                    LogTheme.colors.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (i < columns.lastIndex) {
                            HorizontalDivider(
                                color = LogTheme.colors.outlineVariant.copy(alpha = 0.5f),
                                thickness = LogDimens.listDividerThickness,
                            )
                        }
                    }
                }
            }
        }
    }
}
