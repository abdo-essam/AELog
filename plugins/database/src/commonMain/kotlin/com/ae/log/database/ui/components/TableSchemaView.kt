package com.ae.log.database.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.TableSchema
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun TableSchemaView(
    table: DbTable,
    schema: TableSchema?,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isLoading) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(LogSpacing.x8))
        }
        return
    }

    if (schema == null) {
        EmptyPlaceholder("Schema details not available for ${table.name}")
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x3),
        verticalArrangement = Arrangement.spacedBy(LogSpacing.x3),
    ) {
        // ── Table Summary Header Card ─────────────────────────────────
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LogSpacing.x3),
                colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(LogSpacing.x4),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier =
                            Modifier
                                .size(44.dp)
                                .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.TableChart,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(Modifier.width(14.dp))

                    Column {
                        Text(
                            text = table.name,
                            style = LogTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = LogTheme.colors.onSurface,
                        )
                        Spacer(Modifier.height(2.dp))
                        val rowsMeta = if (table.rowCount >= 0) "${table.rowCount} rows" else "Table"
                        val sizeMeta = DatabaseFormatUtils.formatBytes(table.sizeBytes)
                        Text(
                            text = if (table.sizeBytes > 0) "$rowsMeta • $sizeMeta" else rowsMeta,
                            style = LogTheme.typography.labelSmall,
                            color = LogTheme.colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        // ── Section: Columns ──────────────────────────────────────────
        item {
            Text(
                text = "Columns",
                style = LogTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = LogTheme.colors.onSurface,
                modifier = Modifier.padding(top = LogSpacing.x1),
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(LogSpacing.x3),
                colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(vertical = LogSpacing.x2)) {
                    schema.columns.forEachIndexed { index, col ->
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = LogSpacing.x4, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = col.name,
                                style = LogTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = LogTheme.colors.onSurface,
                                modifier = Modifier.weight(1f),
                            )

                            Text(
                                text = col.type.uppercase(),
                                style = LogTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = LogTheme.colors.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = LogSpacing.x2),
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(LogSpacing.x1),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                if (col.isPrimaryKey) {
                                    PrimaryKeyBadge()
                                }
                                if (col.isNotNull) {
                                    NotNullBadge()
                                }
                            }
                        }

                        if (index < schema.columns.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = LogSpacing.x4),
                                color = LogTheme.colors.outlineVariant,
                                thickness = LogDimens.listDividerThickness,
                            )
                        }
                    }
                }
            }
        }

        // ── Section: Indexes ──────────────────────────────────────────
        if (schema.indexes.isNotEmpty()) {
            item {
                Text(
                    text = "Indexes",
                    style = LogTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = LogTheme.colors.onSurface,
                    modifier = Modifier.padding(top = LogSpacing.x2),
                )
            }

            items(schema.indexes, key = { it.name }) { idx ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                                text = idx.name,
                                style = LogTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace,
                                color = LogTheme.colors.onSurface,
                            )
                            if (idx.isUnique) {
                                UniqueIndexBadge()
                            }
                        }

                        if (idx.columns.isNotEmpty()) {
                            Spacer(Modifier.height(LogSpacing.x1))
                            Text(
                                text = "ON (${idx.columns.joinToString(", ")})",
                                style = LogTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                color = LogTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
