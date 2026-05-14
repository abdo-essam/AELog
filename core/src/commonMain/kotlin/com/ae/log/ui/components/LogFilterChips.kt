package com.ae.log.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ae.log.ui.theme.LogSpacing

/**
 * Shared horizontally-scrollable filter chip row for AELog panels.
 *
 * @param labels List of chip labels.
 * @param selectedIndex Index of the currently selected chip.
 * @param onSelect Called with the new selected index.
 */
@Composable
public fun LogFilterChips(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(LogSpacing.x2),
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(index) },
                label = {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            if (isSelected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                border =
                    FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = MaterialTheme.colorScheme.primary,
                        enabled = true,
                        selected = isSelected,
                    ),
            )
        }
    }
}
