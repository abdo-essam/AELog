package com.ae.log.database

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    public val logRecorder: DatabaseLogRecorder = DatabaseLogRecorder(config.maxLogEntries)

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
        val vm = DatabaseViewModel(inspector, config, context.scope, logRecorder)
        viewModel = vm

        context.scope.launch {
            logRecorder.logs.collect { logs ->
                // Badge surfaces unhandled database errors for quick developer action
                val errorCount = logs.count { !it.isSuccess }
                _badgeCount.value = errorCount
            }
        }
    }

    override fun onClear() {
        viewModel?.clear()
        logRecorder.clear()
    }

    /**
     * Exports a textual dump of discovered databases and tables.
     * Note: If called on Android with file-based inspectors, invoke from a background thread
     * to avoid performing file I/O on the main thread.
     */
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
        val vm = viewModel
        if (vm == null) {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return
        }
        DatabaseContent(viewModel = vm, modifier = modifier)
    }

    public companion object {
        public const val ID: String = "ae_logs_database"
    }
}
