package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ae.log.database.model.QueryResult
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

private const val RUN_QUERY_LABEL = "Run Query"

@Composable
internal fun QueryEditorView(
    db: DbInfo,
    viewModel: DatabaseViewModel,
    sqlQuery: String,
    onSqlQueryChange: (String) -> Unit,
    queryResult: QueryResult?,
    isRunning: Boolean,
    isWriteModeEnabled: Boolean,
    onToggleWriteMode: () -> Unit,
    onRunQuery: () -> Unit,
    tableName: String? = null,
    showBackButton: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(LogTheme.colors.background),
    ) {
        if (showBackButton) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LogSpacing.x3, vertical = LogSpacing.x2),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.popBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = LogTheme.colors.onSurface,
                        )
                    }
                    Spacer(Modifier.width(LogSpacing.x1))
                    Text(
                        text = "Query",
                        style = LogTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = LogTheme.colors.onSurface,
                    )
                }

                IconButton(
                    onClick = onRunQuery,
                    enabled = !isRunning && sqlQuery.isNotBlank(),
                    modifier =
                        Modifier
                            .size(36.dp)
                            .background(LogTheme.colors.primary, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = RUN_QUERY_LABEL,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
            verticalArrangement = Arrangement.spacedBy(LogSpacing.x3),
        ) {
            // ── SQL Code Editor Area ──────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LogSpacing.x3),
                    colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
                ) {
                    Column(modifier = Modifier.padding(LogSpacing.x3)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "SQL Statement",
                                style = LogTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LogTheme.colors.onSurfaceVariant,
                            )

                            FilterChip(
                                selected = isWriteModeEnabled,
                                onClick = onToggleWriteMode,
                                label = {
                                    Text(
                                        text = if (isWriteModeEnabled) "Write: ON" else "Read-Only",
                                        style = LogTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                    )
                                },
                                colors =
                                    FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = LogTheme.colors.errorContainer,
                                        selectedLabelColor = LogTheme.colors.onErrorContainer,
                                    ),
                            )
                        }

                        Spacer(Modifier.height(LogSpacing.x2))

                        OutlinedTextField(
                            value = sqlQuery,
                            onValueChange = onSqlQueryChange,
                            placeholder = {
                                Text(
                                    text = "SELECT * FROM \"${tableName ?: "table"}\" LIMIT 10;",
                                    style = LogTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                    color = LogTheme.colors.onSurfaceVariant,
                                )
                            },
                            textStyle = LogTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = LogTheme.colors.primary,
                                    unfocusedBorderColor = LogTheme.colors.outlineVariant,
                                    focusedContainerColor = LogTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                                    unfocusedContainerColor = LogTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                                ),
                            shape = RoundedCornerShape(LogSpacing.x2),
                        )
                    }
                }
            }

            // ── Quick Snippet Helpers ─────────────────────────────────
            item {
                val targetTable = tableName ?: "table"
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(LogSpacing.x1_5),
                ) {
                    listOf(
                        "SELECT *" to "SELECT * FROM \"$targetTable\" LIMIT 20;",
                        "COUNT(*)" to "SELECT COUNT(*) FROM \"$targetTable\";",
                        "WHERE" to "SELECT * FROM \"$targetTable\" WHERE ;",
                        "ORDER BY" to "SELECT * FROM \"$targetTable\" ORDER BY rowid DESC LIMIT 20;",
                        "PRAGMA" to "PRAGMA table_info(\"$targetTable\");",
                    ).forEach { (label, sql) ->
                        FilterChip(
                            selected = false,
                            onClick = { onSqlQueryChange(sql) },
                            label = { Text(label, style = LogTheme.typography.labelSmall) },
                        )
                    }
                }
            }

            // ── Large Run Query Button ────────────────────────────────
            item {
                Button(
                    onClick = onRunQuery,
                    enabled = !isRunning && sqlQuery.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = LogTheme.colors.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    if (isRunning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White,
                        )
                        Spacer(Modifier.width(LogSpacing.x2))
                        Text("Executing…", style = LogTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(LogSpacing.x2))
                        Text(
                            text = RUN_QUERY_LABEL,
                            style = LogTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            // ── Query Results Section ─────────────────────────────────
            item {
                if (queryResult != null) {
                    val headerText =
                        when {
                            !queryResult.isSuccess -> "Query Error"
                            queryResult.affectedRows != null ->
                                "Result: ${queryResult.affectedRows} row(s) affected • ${queryResult.executionDurationMs} ms"
                            else ->
                                "Result (${queryResult.rows.size} rows • ${queryResult.executionDurationMs} ms)"
                        }

                    Text(
                        text = headerText,
                        style = LogTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (!queryResult.isSuccess) LogTheme.colors.error else LogTheme.colors.onSurface,
                        modifier = Modifier.padding(top = LogSpacing.x1),
                    )
                }
            }

            item {
                Card(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                    shape = RoundedCornerShape(LogSpacing.x3),
                    colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
                ) {
                    when {
                        queryResult == null -> {
                            EmptyPlaceholder("Execute a query above to inspect results.")
                        }

                        !queryResult.isSuccess -> {
                            Column(modifier = Modifier.fillMaxSize().padding(LogSpacing.x4)) {
                                Text(
                                    text = queryResult.errorMessage ?: "Unknown SQL error",
                                    style = LogTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = LogTheme.colors.error,
                                    lineHeight = 18.sp,
                                )
                            }
                        }

                        queryResult.affectedRows != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text =
                                        "Statement executed successfully.\n" +
                                            "${queryResult.affectedRows} row(s) updated.",
                                    style = LogTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LogTheme.colors.primary,
                                )
                            }
                        }

                        queryResult.rows.isEmpty() -> {
                            EmptyPlaceholder("0 rows returned.")
                        }

                        else -> {
                            QueryResultGrid(queryResult = queryResult)
                        }
                    }
                }
                Spacer(Modifier.height(LogSpacing.x4))
            }
        }
    }
}

@Composable
private fun QueryResultGrid(queryResult: QueryResult) {
    val hScroll = rememberScrollState()

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .horizontalScroll(hScroll),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = LogSpacing.x2),
        ) {
            item {
                Row(
                    modifier =
                        Modifier
                            .background(LogTheme.colors.surfaceVariant)
                            .padding(vertical = LogSpacing.x2),
                ) {
                    Text(
                        text = "#",
                        modifier = Modifier.width(40.dp).padding(horizontal = LogSpacing.x2),
                        style = LogTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = LogTheme.colors.primary,
                    )
                    queryResult.columns.forEach { col ->
                        Text(
                            text = col,
                            modifier = Modifier.width(120.dp).padding(horizontal = LogSpacing.x2),
                            style = LogTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = LogTheme.colors.onSurfaceVariant,
                        )
                    }
                }
                HorizontalDivider(color = LogTheme.colors.outlineVariant, thickness = LogDimens.listDividerThickness)
            }

            itemsIndexed(queryResult.rows) { index, row ->
                Row(
                    modifier = Modifier.padding(vertical = LogSpacing.x2),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "${index + 1}",
                        modifier = Modifier.width(40.dp).padding(horizontal = LogSpacing.x2),
                        style = LogTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = LogTheme.colors.onSurfaceVariant,
                    )
                    queryResult.columns.indices.forEach { i ->
                        val value = row.getOrNull(i)
                        Text(
                            text = value ?: "null",
                            modifier = Modifier.width(120.dp).padding(horizontal = LogSpacing.x2),
                            style = LogTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontStyle = if (value == null) FontStyle.Italic else FontStyle.Normal,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color =
                                if (value ==
                                    null
                                ) {
                                    LogTheme.colors.error.copy(alpha = 0.7f)
                                } else {
                                    LogTheme.colors.onSurface
                                },
                        )
                    }
                }
                if (index < queryResult.rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = LogSpacing.x3),
                        color = LogTheme.colors.outlineVariant.copy(alpha = 0.5f),
                        thickness = LogDimens.listDividerThickness,
                    )
                }
            }
        }
    }
}
