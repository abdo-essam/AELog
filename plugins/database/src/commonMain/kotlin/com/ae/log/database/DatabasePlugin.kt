package com.ae.log.database

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.inspector.DatabaseInspector
import com.ae.log.database.inspector.createPlatformDatabaseInspector
import com.ae.log.database.ui.DatabaseContent
import com.ae.log.database.ui.DatabaseViewModel
import com.ae.log.plugin.PluginContext
import com.ae.log.plugin.UIPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * AELog plugin for inspecting the host application's SQLite databases directly inside the AELog panel.
 *
 * ## Features
 * - Auto-discovery of databases across platforms (Android, iOS sandbox, JVM, and custom registered).
 * - Live table list with row counts and schema information.
 * - Paginated table row browser.
 * - Interactive SQL query console with safe read-only default and opt-in write mode.
 * - WAL (Write-Ahead Logging) consistent snapshot queries without lock conflicts.
 * - Encryption detection and passphrase injection for SQLCipher databases.
 *
 * ## Installation
 * ```kotlin
 * AELog.configure {
 *     plugin(DatabasePlugin())
 * }
 * ```
 */
public class DatabasePlugin(
    public val config: DatabasePluginConfig = DatabasePluginConfig(),
    inspector: DatabaseInspector = createPlatformDatabaseInspector(config),
) : UIPlugin {
    public var inspector: DatabaseInspector = inspector
        set(value) {
            field = value
            viewModel?.updateInspector(value)
        }
    override val id: String = ID
    override val name: String = "Database"
    override val icon: @Composable () -> Unit = { Icon(Icons.Default.Storage, contentDescription = null) }

    private val _badgeCount = MutableStateFlow(0)
    override val badgeCount: StateFlow<Int> = _badgeCount

    @kotlin.concurrent.Volatile
    private var viewModel: DatabaseViewModel? = null

    override fun onAttach(context: PluginContext) {
        val vm = DatabaseViewModel(inspector, config, context.scope)
        viewModel = vm

        context.scope.launch {
            vm.databases.collect { list ->
                _badgeCount.value = list.size
            }
        }
    }

    override fun onClear() {
        viewModel?.clear()
    }

    override fun export(): String {
        val databases = inspector.listDatabases()
        if (databases.isEmpty()) return "No databases found."

        return buildString {
            appendLine("=== Databases (${databases.size}) ===")
            databases.forEach { db ->
                appendLine("Database: ${db.name}")
                appendLine("Path: ${db.path}")
                appendLine("Encrypted: ${db.isEncrypted}")
                val tables = inspector.listTables(db)
                appendLine("Tables (${tables.size}):")
                tables.forEach { t ->
                    appendLine("  - ${t.name} (${if (t.rowCount >= 0) "${t.rowCount} rows" else "unknown rows"})")
                }
                appendLine()
            }
        }.trimEnd()
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        DatabaseContent(viewModel = vm, modifier = modifier)
    }

    public companion object {
        public const val ID: String = "ae_logs_database"
    }
}
