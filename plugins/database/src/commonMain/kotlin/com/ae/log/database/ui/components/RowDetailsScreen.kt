package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.TableRows
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.ui.components.LogItemCard
import com.ae.log.ui.components.LogKeyValueItem
import com.ae.log.ui.components.LogScreenHeader
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
internal fun RowDetailsScreen(
    db: DbInfo,
    table: DbTable,
    row: Map<String, String?>,
    rowIndex: Int,
    viewModel: DatabaseViewModel,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copyNotice by remember { mutableStateOf<String?>(null) }

    fun showNotice(msg: String) {
        copyNotice = msg
        scope.launch {
            delay(2000)
            copyNotice = null
        }
    }

    val idKey = row.keys.firstOrNull { it.equals("id", ignoreCase = true) || it.endsWith("_id", ignoreCase = true) }
    val idVal = if (idKey != null) row[idKey] else "${rowIndex + 1}"

    val titleVal = row.entries.firstOrNull { (k, v) ->
        !k.equals("id", ignoreCase = true) && !k.endsWith("_id", ignoreCase = true) && !v.isNullOrBlank()
    }?.value ?: "${table.name} #${rowIndex + 1}"

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogTheme.colors.background),
    ) {
        // ── Top Bar ───────────────────────────────────────────────────
        LogScreenHeader(
            title = "${table.name.replaceFirstChar { it.uppercase() }} Details",
            onBackClick = { viewModel.popBack() },
            actions = {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(DatabaseFormatUtils.rowToJson(row)))
                        showNotice("JSON copied to clipboard")
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy JSON",
                        tint = LogTheme.colors.primary,
                    )
                }
            },
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
            verticalArrangement = Arrangement.spacedBy(LogSpacing.x3),
        ) {
            // ── Primary Summary Card ──────────────────────────────────
            item {
                LogItemCard(
                    title = titleVal,
                    subtitle = if (idKey != null) "$idKey: $idVal" else "Row #${rowIndex + 1}",
                    icon = {
                        Icon(
                            imageVector = Icons.Default.TableRows,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(26.dp),
                        )
                    },
                    iconContainerColor = Color(0xFFE3F2FD),
                )
            }

            // ── Notice feedback ───────────────────────────────────────
            if (copyNotice != null) {
                item {
                    Text(
                        text = copyNotice!!,
                        style = LogTheme.typography.labelSmall,
                        color = LogTheme.colors.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            // ── Key-Value Attributes List ─────────────────────────────
            item {
                val entries = row.entries.toList()
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LogDimens.cardCornerRadius),
                    colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = LogSpacing.x2)) {
                        entries.forEachIndexed { index, (key, value) ->
                            LogKeyValueItem(
                                key = key,
                                value = value,
                                isDividerVisible = index < entries.lastIndex,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(value ?: ""))
                                    showNotice("Copied \"$key\"")
                                },
                            )
                        }
                    }
                }
            }

            // ── Action Buttons ────────────────────────────────────────
            item {
                Spacer(Modifier.height(LogSpacing.x1))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LogSpacing.x3),
                ) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(DatabaseFormatUtils.rowToJson(row)))
                            showNotice("JSON copied to clipboard")
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.DataObject,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(LogSpacing.x1_5))
                        Text("Copy JSON", style = LogTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = {
                            val sql = DatabaseFormatUtils.rowToSqlInsert(table.name, row)
                            clipboard.setText(AnnotatedString(sql))
                            showNotice("SQL INSERT copied to clipboard")
                        },
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(LogSpacing.x1_5))
                        Text("Copy SQL", style = LogTheme.typography.labelMedium)
                    }
                }

                Spacer(Modifier.height(LogSpacing.x2))

                Button(
                    onClick = {
                        viewModel.toggleWriteMode()
                        showNotice(if (!viewModel.isWriteModeEnabled.value) "Edit Mode Enabled" else "Read-Only Mode")
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (viewModel.isWriteModeEnabled.value)
                            LogTheme.colors.error
                        else
                            LogTheme.colors.primary,
                    ),
                ) {
                    Text(
                        text = if (viewModel.isWriteModeEnabled.value) "Edit (Write Enabled)" else "Edit (Read Only)",
                        style = LogTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(Modifier.height(LogSpacing.x4))
            }
        }
    }
}
