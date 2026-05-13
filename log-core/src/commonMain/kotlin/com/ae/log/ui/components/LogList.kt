package com.ae.log.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogSpacing

@Composable
public fun <T> LogList(
    items: List<T>,
    itemLabel: String,
    searchQuery: String,
    searchPlaceholder: String,
    onSearchChange: (String) -> Unit,
    filterLabels: List<String>,
    selectedFilterIndex: Int,
    onFilterSelect: (Int) -> Unit,
    onClearAll: () -> Unit,
    onCopyAll: (() -> Unit)? = null,
    emptyMessage: String = "No items",
    emptyQueryMessage: String = "No results for query",
    listState: LazyListState,
    itemKey: (T) -> Any,
    modifier: Modifier = Modifier,
    itemContent: @Composable (index: Int, item: T) -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LogHeader(
            itemCount = items.size,
            itemLabel = itemLabel,
            onClearAll = onClearAll,
            actions = {
                if (onCopyAll != null) {
                    Button(
                        onClick = onCopyAll,
                        contentPadding = PaddingValues(horizontal = LogSpacing.x3, vertical = LogSpacing.x1),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy all",
                            modifier = Modifier.size(LogSpacing.x4),
                        )
                        Spacer(modifier = Modifier.width(LogSpacing.x1))
                        Text("Copy All", style = MaterialTheme.typography.labelSmall)
                    }
                }
            },
        )

        Spacer(modifier = Modifier.height(LogSpacing.x3))

        LogSearchBar(
            query = searchQuery,
            onQueryChange = onSearchChange,
            placeholder = searchPlaceholder,
            modifier = Modifier.padding(horizontal = LogSpacing.x5),
        )

        Spacer(modifier = Modifier.height(LogSpacing.x3))

        LogFilterChips(
            labels = filterLabels,
            selectedIndex = selectedFilterIndex,
            onSelect = onFilterSelect,
            modifier = Modifier.padding(horizontal = LogSpacing.x5),
        )

        Spacer(modifier = Modifier.height(LogSpacing.x3))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (items.isEmpty()) {
                val msg = if (searchQuery.isNotEmpty()) emptyQueryMessage else emptyMessage
                EmptyPlaceholder(msg)
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = LogSpacing.x5),
                    shape = RoundedCornerShape(LogSpacing.x3),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = LogSpacing.x2),
                    ) {
                        itemsIndexed(items = items, key = { _, i -> itemKey(i) }) { index, item ->
                            itemContent(index, item)
                            if (index < items.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = LogSpacing.x3),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
public fun EmptyPlaceholder(message: String) {
    Box(Modifier.fillMaxSize().padding(LogSpacing.x8), Alignment.Center) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
public fun ExpandedDetails(
    bgColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    onCopy: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = LogSpacing.x3),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(LogSpacing.x2),
            colors = CardDefaults.cardColors(containerColor = bgColor),
        ) {
            Column(modifier = Modifier.padding(LogSpacing.x3)) {
                content()
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = LogSpacing.x2),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onCopy) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(LogSpacing.x4),
                )
                Spacer(modifier = Modifier.width(LogSpacing.x1))
                Text(
                    text = "Copy",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
