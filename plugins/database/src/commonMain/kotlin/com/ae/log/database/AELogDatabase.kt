package com.ae.log.database

import com.ae.log.AELog
import com.ae.log.database.model.DatabaseLogEntry
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Public convenience API accessible via `AELog.database`.
 *
 * ### Example usage:
 * ```kotlin
 * // List all discovered databases
 * val databases = AELog.database.listDatabases()
 *
 * // Run a quick query
 * val result = AELog.database.query(
 *     dbName = "app_database.db",
 *     sql = "SELECT * FROM users LIMIT 10",
 * )
 *
 * // Log a query executed by your app
 * AELog.database.logQuery(
 *     databaseName = "app_database.db",
 *     sql = "SELECT * FROM users WHERE active = 1",
 *     durationMs = 4L,
 * )
 * ```
 */
public val AELog.database: DatabaseProxy
    get() = DatabaseProxy

public object DatabaseProxy {
    /**
     * Lists all databases discovered or registered with the database plugin.
     */
    public fun listDatabases(): List<DbInfo> =
        AELog.getPlugin<DatabasePlugin>()?.inspector?.listDatabases() ?: emptyList()

    /**
     * Lists tables for the given database name.
     */
    public fun listTables(dbName: String): List<DbTable> {
        val plugin = AELog.getPlugin<DatabasePlugin>() ?: return emptyList()
        val dbInfo = plugin.inspector.listDatabases().firstOrNull { it.name == dbName } ?: return emptyList()
        return plugin.inspector.listTables(dbInfo)
    }

    /**
     * Executes a SQL statement against the specified database.
     */
    public fun query(
        dbName: String,
        sql: String,
        allowWrite: Boolean = false,
    ): QueryResult {
        val plugin =
            AELog.getPlugin<DatabasePlugin>()
                ?: return QueryResult.error("DatabasePlugin is not installed in AELog")
        val dbInfo =
            plugin.inspector.listDatabases().firstOrNull { it.name == dbName }
                ?: return QueryResult.error("Database '$dbName' not found")
        val result = plugin.inspector.query(
            dbInfo = dbInfo,
            sql = sql,
            allowWrite = allowWrite,
        )
        // Record into database log history
        DatabaseLogRecorder.record(
            databaseName = dbName,
            sql = sql,
            durationMs = result.executionDurationMs,
            isSuccess = result.isSuccess,
            errorMessage = result.errorMessage,
            affectedRows = result.affectedRows,
        )
        return result
    }

    /**
     * Records a database query or operation in the AELog Database Logs view.
     */
    public fun logQuery(
        databaseName: String,
        sql: String,
        durationMs: Long,
        tableName: String? = null,
        isSuccess: Boolean = true,
        errorMessage: String? = null,
        affectedRows: Long? = null,
    ) {
        DatabaseLogRecorder.record(
            databaseName = databaseName,
            sql = sql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = isSuccess,
            errorMessage = errorMessage,
            affectedRows = affectedRows,
        )
    }

    /**
     * Live stream of recent database operation logs.
     */
    public val logs: StateFlow<List<DatabaseLogEntry>>
        get() = DatabaseLogRecorder.logs

    /**
     * Manually registers a database with the inspector (useful on desktop, iOS, or WASM).
     */
    public fun registerDatabase(dbInfo: DbInfo) {
        AELog.getPlugin<DatabasePlugin>()?.inspector?.registerDatabase(dbInfo)
    }

    /**
     * Manually registers a database by name and path.
     */
    public fun registerDatabase(
        name: String,
        path: String,
        isEncrypted: Boolean = false,
    ) {
        registerDatabase(DbInfo(name = name, path = path, isEncrypted = isEncrypted))
    }
}
