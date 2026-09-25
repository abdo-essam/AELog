package com.ae.log.database.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.ae.log.database.model.QueryResult
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun SqlConsoleView(
    sqlQuery: String,
    onSqlQueryChange: (String) -> Unit,
    queryResult: QueryResult?,
    isRunning: Boolean,
    isWriteModeEnabled: Boolean,
    onToggleWriteMode: () -> Unit,
    onRunQuery: () -> Unit,
    selectedTableName: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x3),
    ) {
        // ── SQL input ─────────────────────────────────────────────────
        OutlinedTextField(
            value = sqlQuery,
            onValueChange = onSqlQueryChange,
            placeholder = {
                Text(
                    text = "SELECT * FROM ${selectedTableName ?: "table"} LIMIT 10",
                    style = LogTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    color = LogTheme.colors.onSurfaceVariant,
                )
            },
            textStyle = LogTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(LogSpacing.x12 + LogSpacing.x12 + LogSpacing.x4),
            // ~112dp
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LogTheme.colors.primary,
                    unfocusedBorderColor = LogTheme.colors.outline,
                    focusedContainerColor = LogTheme.colors.surface,
                    unfocusedContainerColor = LogTheme.colors.surface,
                ),
            shape = RoundedCornerShape(LogSpacing.x3),
        )

        Spacer(Modifier.height(LogSpacing.x3))

        // ── Quick snippet chips ────────────────────────────────────────
        if (selectedTableName != null) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(LogSpacing.x1_5),
            ) {
                listOf(
                    "SELECT 50" to "SELECT * FROM \"$selectedTableName\" LIMIT 50;",
                    "COUNT(*)" to "SELECT COUNT(*) FROM \"$selectedTableName\";",
                    "SCHEMA" to "PRAGMA table_info(\"$selectedTableName\");",
                    "LATEST 20" to "SELECT * FROM \"$selectedTableName\" ORDER BY rowid DESC LIMIT 20;",
                ).forEach { (label, sql) ->
                    FilterChip(
                        selected = false,
                        onClick = { onSqlQueryChange(sql) },
                        label = { Text(label, style = LogTheme.typography.labelSmall) },
                    )
                }
            }
            Spacer(Modifier.height(LogSpacing.x3))
        }

        // ── Controls row ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = isWriteModeEnabled,
                onClick = onToggleWriteMode,
                label = {
                    Text(
                        text = if (isWriteModeEnabled) "Write: ON" else "Read-Only",
                        style = LogTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                colors =
                    FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LogTheme.colors.errorContainer,
                        selectedLabelColor = LogTheme.colors.onErrorContainer,
                    ),
            )

            Button(
                onClick = onRunQuery,
                enabled = !isRunning && sqlQuery.isNotBlank(),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = LogTheme.colors.primary,
                    ),
                shape = RoundedCornerShape(LogSpacing.x2),
                contentPadding =
                    PaddingValues(
                        horizontal = LogSpacing.x4,
                        vertical = LogSpacing.x2,
                    ),
            ) {
                if (isRunning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(LogSpacing.x4),
                        strokeWidth = LogSpacing.x1 / 2,
                        color = LogTheme.colors.onPrimary,
                    )
                    Spacer(Modifier.width(LogSpacing.x2))
                    Text("Running…", style = LogTheme.typography.labelMedium)
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Run Query",
                        modifier = Modifier.size(LogSpacing.x4),
                    )
                    Spacer(Modifier.width(LogSpacing.x1))
                    Text("Run", style = LogTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(LogSpacing.x3))

        // ── Result area ───────────────────────────────────────────────
        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            shape = RoundedCornerShape(LogSpacing.x3),
            colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
        ) {
            when {
                queryResult == null -> {
                    EmptyPlaceholder("Execute a query to see results")
                }

                !queryResult.isSuccess -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(LogSpacing.x4),
                    ) {
                        Text(
                            text = "Query Error",
                            style = LogTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = LogTheme.colors.error,
                        )
                        Spacer(Modifier.height(LogSpacing.x2))
                        Text(
                            text = queryResult.errorMessage ?: "Unknown error",
                            style = LogTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = LogTheme.colors.error,
                        )
                    }
                }

                queryResult.affectedRows != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Query executed successfully",
                                style = LogTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = LogTheme.colors.primary,
                            )
                            Spacer(Modifier.height(LogSpacing.x1))
                            Text(
                                text =
                                    "${queryResult.affectedRows} row(s) affected · " +
                                        "${queryResult.executionDurationMs} ms",
                                style = LogTheme.typography.labelSmall,
                                color = LogTheme.colors.onSurfaceVariant,
                            )
                        }
                    }
                }

                else -> {
                    TableDataGrid(
                        result = queryResult,
                        isLoading = false,
                        page = 0,
                        onNextPage = {},
                        onPreviousPage = {},
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
