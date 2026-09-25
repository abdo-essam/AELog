package com.ae.log.database.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.components.LogSearchBar
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun TableListView(
    tables: List<com.ae.log.database.model.DbTable>,
    selectedTable: com.ae.log.database.model.DbTable?,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelectTable: (com.ae.log.database.model.DbTable) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filtered =
        tables.filter {
            searchQuery.isBlank() || it.name.contains(searchQuery, ignoreCase = true)
        }

    Column(modifier = modifier.fillMaxSize()) {
        // Use AELog's shared LogSearchBar — consistent with logs/network plugins
        LogSearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = "Search tables…",
            modifier =
                Modifier.padding(
                    horizontal = LogSpacing.x3,
                    vertical = LogSpacing.x2,
                ),
        )

        if (filtered.isEmpty()) {
            EmptyPlaceholder(
                message =
                    if (searchQuery.isBlank()) {
                        "No tables found"
                    } else {
                        "No match for \"$searchQuery\""
                    },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = LogSpacing.x2),
            ) {
                items(filtered, key = { it.name }) { table ->
                    val isSelected = selectedTable?.name == table.name

                    TableListItem(
                        table = table,
                        isSelected = isSelected,
                        onSelect = { onSelectTable(table) },
                    )

                    if (table != filtered.last()) {
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

@Composable
private fun TableListItem(
    table: com.ae.log.database.model.DbTable,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = "Select table ${table.name}",
                ) { onSelect() }
                // Match AELog item padding: x4 horizontal, x3 vertical
                .padding(horizontal = LogSpacing.x4, vertical = LogSpacing.x3),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.TableChart,
            contentDescription = null,
            modifier = Modifier.size(LogSpacing.x4),
            tint = if (isSelected) LogTheme.colors.primary else LogTheme.colors.onSurfaceVariant,
        )

        Spacer(Modifier.width(LogSpacing.x3))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = table.name,
                style = LogTheme.typography.bodySmall,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isSelected) LogTheme.colors.primary else LogTheme.colors.onSurface,
            )
            if (table.isSystemTable) {
                Text(
                    text = "system table",
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }
        }

        if (table.rowCount >= 0) {
            Spacer(Modifier.width(LogSpacing.x2))
            Text(
                text = "${table.rowCount}",
                style = LogTheme.typography.labelSmall,
                color = if (isSelected) LogTheme.colors.primary else LogTheme.colors.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}
