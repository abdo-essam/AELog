package com.ae.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Standard pagination bar used at the bottom of data grids and paged lists.
 *
 * @param page Current 0-based page index.
 * @param pageSize Number of rows per page.
 * @param rowCount Number of rows in the current loaded page.
 * @param totalRowCount Total rows in the dataset if known, or -1.
 * @param onPreviousPage Callback to navigate to previous page.
 * @param onNextPage Callback to navigate to next page.
 * @param onPageSizeChange Optional callback when user changes rows per page.
 * @param availablePageSizes List of selectable page sizes.
 */
@Composable
public fun LogPaginationBar(
    page: Int,
    pageSize: Int,
    rowCount: Int,
    totalRowCount: Long = -1L,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onPageSizeChange: ((Int) -> Unit)? = null,
    availablePageSizes: List<Int> = listOf(10, 20, 50),
    modifier: Modifier = Modifier,
) {
    var sizeMenuExpanded by remember { mutableStateOf(false) }

    val totalPages =
        if (totalRowCount > 0) {
            ((totalRowCount + pageSize - 1) / pageSize).toInt().coerceAtLeast(1)
        } else {
            (page + 1).coerceAtLeast(1)
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Prev < page/total > Next
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPreviousPage, enabled = page > 0) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous page",
                    tint =
                        if (page >
                            0
                        ) {
                            LogTheme.colors.primary
                        } else {
                            LogTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                    modifier = Modifier.size(24.dp),
                )
            }

            Text(
                text = "${page + 1}/$totalPages",
                style = LogTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = LogTheme.colors.onSurface,
                modifier = Modifier.padding(horizontal = LogSpacing.x1),
            )

            IconButton(onClick = onNextPage, enabled = rowCount >= pageSize) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next page",
                    tint =
                        if (rowCount >=
                            pageSize
                        ) {
                            LogTheme.colors.primary
                        } else {
                            LogTheme.colors.onSurfaceVariant.copy(alpha = 0.4f)
                        },
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        // Rows per page dropdown
        if (onPageSizeChange != null) {
            Box {
                Row(
                    modifier =
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LogTheme.colors.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { sizeMenuExpanded = true }
                            .padding(horizontal = LogSpacing.x2_5, vertical = LogSpacing.x1_5),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Rows: $pageSize",
                        style = LogTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = LogTheme.colors.onSurface,
                    )
                    Spacer(Modifier.width(LogSpacing.x1))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Change rows per page",
                        tint = LogTheme.colors.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }

                DropdownMenu(
                    expanded = sizeMenuExpanded,
                    onDismissRequest = { sizeMenuExpanded = false },
                    modifier = Modifier.background(LogTheme.colors.surfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    availablePageSizes.forEach { s ->
                        val isSelected = s == pageSize
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$s rows per page",
                                    style = LogTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) LogTheme.colors.primary else LogTheme.colors.onSurface,
                                )
                            },
                            trailingIcon = {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = LogTheme.colors.primary,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            },
                            onClick = {
                                onPageSizeChange(s)
                                sizeMenuExpanded = false
                            },
                            colors =
                                MenuDefaults.itemColors(
                                    textColor = LogTheme.colors.onSurface,
                                ),
                        )
                    }
                }
            }
        }
    }
}
