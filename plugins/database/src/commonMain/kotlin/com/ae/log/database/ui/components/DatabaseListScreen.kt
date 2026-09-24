package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.database.model.DbInfo
import com.ae.log.database.ui.DatabaseDestination
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.ui.components.EmptyPlaceholder
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun DatabaseListScreen(
    viewModel: DatabaseViewModel,
    databases: List<DbInfo>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LogTheme.colors.background),
    ) {
        // ── Top Bar ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LogSpacing.x5, vertical = LogSpacing.x3),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = "Database",
                    style = LogTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = LogTheme.colors.onSurface,
                )
                Text(
                    text = "Inspect SQLite & Room databases",
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
            }

            IconButton(onClick = { viewModel.refreshDatabases() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh databases",
                    tint = LogTheme.colors.primary,
                    modifier = Modifier.size(LogSpacing.x5),
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = LogSpacing.x5, vertical = LogSpacing.x2),
            verticalArrangement = Arrangement.spacedBy(LogSpacing.x3),
        ) {
            // ── Section: Connected Databases ──────────────────────────
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(top = LogSpacing.x1)) {
                    Text(
                        text = "Connected Databases",
                        style = LogTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = LogTheme.colors.onSurface,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Tap a database to view tables and inspect data.",
                        style = LogTheme.typography.bodySmall,
                        color = LogTheme.colors.onSurfaceVariant,
                    )
                }
            }

            if (databases.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(LogSpacing.x3),
                        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
                    ) {
                        EmptyPlaceholder("No SQLite or Room databases found on device.")
                    }
                }
            } else {
                items(databases, key = { it.path }) { db ->
                    DatabaseCardItem(
                        db = db,
                        onClick = { viewModel.selectDatabase(db, navigate = true) },
                    )
                }
            }

            // ── Database Logs Entry Card ──────────────────────────────
            item {
                Spacer(Modifier.height(LogSpacing.x1))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(LogSpacing.x3))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            viewModel.navigateTo(DatabaseDestination.DatabaseLogs(null))
                        },
                    shape = RoundedCornerShape(LogSpacing.x3),
                    colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LogSpacing.x4),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(Color(0xFFE8EAF6), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF3F51B5),
                                modifier = Modifier.size(24.dp),
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Database Logs",
                                style = LogTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = LogTheme.colors.onSurface,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "View recent queries, writes, and operations.",
                                style = LogTheme.typography.bodySmall,
                                color = LogTheme.colors.onSurfaceVariant,
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = LogTheme.colors.onSurfaceVariant,
                            modifier = Modifier.size(LogSpacing.x5),
                        )
                    }
                }
            }

            // ── Security Notice Banner ────────────────────────────────
            item {
                Spacer(Modifier.height(LogSpacing.x1))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LogSpacing.x3),
                    colors = CardDefaults.cardColors(
                        containerColor = LogTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(LogSpacing.x4),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = LogTheme.colors.primary,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp),
                        )

                        Spacer(Modifier.width(LogSpacing.x3))

                        Column {
                            Text(
                                text = "Security State",
                                style = LogTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LogTheme.colors.primary,
                            )
                            Spacer(Modifier.height(LogSpacing.x1))
                            Text(
                                text = "Database inspection is active in debug mode. Sensitive columns and write operations can be locked or restricted in DatabasePluginConfig.",
                                style = LogTheme.typography.bodySmall,
                                color = LogTheme.colors.onSurfaceVariant,
                                lineHeight = 16.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DatabaseCardItem(
    db: DbInfo,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(LogSpacing.x3))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onClick() },
        shape = RoundedCornerShape(LogSpacing.x3),
        colors = CardDefaults.cardColors(containerColor = LogTheme.colors.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LogSpacing.x4),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFE3F2FD), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (db.isEncrypted) Icons.Default.Lock else Icons.Default.Storage,
                    contentDescription = null,
                    tint = Color(0xFF1976D2),
                    modifier = Modifier.size(24.dp),
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = db.name,
                    style = LogTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = LogTheme.colors.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = db.engine,
                    style = LogTheme.typography.bodySmall,
                    color = LogTheme.colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(2.dp))
                val tablesMeta = if (db.tableCount >= 0) "${db.tableCount} tables" else "Tables"
                val sizeMeta = DatabaseFormatUtils.formatBytes(db.sizeBytes)
                Text(
                    text = "$tablesMeta • $sizeMeta",
                    style = LogTheme.typography.labelSmall,
                    color = LogTheme.colors.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = LogTheme.colors.onSurfaceVariant,
                modifier = Modifier.size(LogSpacing.x5),
            )
        }
    }
}
