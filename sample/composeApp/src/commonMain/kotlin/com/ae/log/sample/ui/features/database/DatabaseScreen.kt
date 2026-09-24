package com.ae.log.sample.ui.features.database

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.AELog
import com.ae.log.database.database
import com.ae.log.database.model.DbInfo
import com.ae.log.sample.deleteSampleDatabase
import com.ae.log.sample.ensureSampleDatabaseExists
import com.ae.log.sample.insertSampleUser
import com.ae.log.sample.ui.components.ActionButton
import com.ae.log.sample.ui.components.ActionCard
import com.ae.log.sample.ui.components.SectionHeader
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScreen() {
    var detectedDatabases by remember { mutableStateOf<List<DbInfo>>(emptyList()) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        detectedDatabases = AELog.database.listDatabases()
    }

    LaunchedEffect(Unit) {
        ensureSampleDatabaseExists()
        refresh()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Database Inspector") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Auto-Discovery ───────────────────────────────────────
            item {
                SectionHeader("Discovered Databases")
                ActionCard(
                    title = "Automatic Device Scanner",
                    description =
                        "AELog automatically scans SQLite database files in the app's databases directory, " +
                            "sandbox, and files directory.",
                ) {
                    ActionButton("Rescan Databases", MaterialTheme.colorScheme.primary) {
                        refresh()
                        statusMessage = "Scanned: found ${detectedDatabases.size} database(s)."
                    }

                    if (detectedDatabases.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text =
                                "Detected (${detectedDatabases.size}): " +
                                    detectedDatabases.joinToString {
                                        "${it.name} (${if (it.sizeBytes > 0) "${it.sizeBytes} B" else "active"})"
                                    },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // ── Sample Database Operations ────────────────────────────
            item {
                SectionHeader("Live Database Actions")
                ActionCard(
                    title = "shop_sample.db",
                    description =
                        "Create real SQLite tables and insert live rows to test live data updates in the AELog panel.",
                ) {
                    ActionButton("Seed / Recreate Database", Color(0xFF4CAF50)) {
                        val result = ensureSampleDatabaseExists()
                        refresh()
                        statusMessage = result
                    }
                    Spacer(Modifier.height(8.dp))

                    ActionButton("Insert Random User Record", Color(0xFF2196F3)) {
                        val randomId = Random.nextInt(100, 999)
                        val roles = listOf("Admin", "Developer", "Designer", "Manager", "Analyst")
                        val success =
                            insertSampleUser(
                                name = "User_$randomId",
                                email = "user$randomId@example.com",
                                role = roles.random(),
                            )
                        refresh()
                        statusMessage =
                            if (success) "Inserted User_$randomId into 'users' table" else "Failed to insert"
                    }
                    Spacer(Modifier.height(8.dp))

                    ActionButton("Delete Database", Color(0xFFE53935)) {
                        val deleted = deleteSampleDatabase()
                        refresh()
                        statusMessage = if (deleted) "Deleted shop_sample.db" else "Failed to delete"
                    }
                }
            }

            // ── Status feedback ───────────────────────────────────────
            if (statusMessage != null) {
                item {
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // ── Interactive UI ───────────────────────────────────────
            item {
                SectionHeader("AELog Panel Inspector")
                ActionCard(
                    title = "In-App Database Viewer",
                    description =
                        "Open the floating overlay to browse tables, view rows, " +
                            "and execute live SQL statements.",
                ) {
                    ActionButton("Open AELog Database Tab", Color(0xFF00897B)) {
                        AELog.show()
                    }
                }
            }
        }
    }
}
