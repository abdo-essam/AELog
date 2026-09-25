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
private const val DEFAULT_DB_NAME = "app.db"

@PublishedApi
internal const val DEFAULT_ERROR_MESSAGE: String = "Database error"

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
        return plugin.inspector.query(
            dbInfo = dbInfo,
            sql = sql,
            allowWrite = allowWrite,
        )
    }

    /**
     * Records a database query or operation in the AELog Database Logs view.
     */
    public fun logQuery(
        databaseName: String,
        sql: String,
        durationMs: Long = 0L,
        tableName: String? = null,
        isSuccess: Boolean = true,
        errorMessage: String? = null,
        affectedRows: Long? = null,
        bindArgs: List<Any?> = emptyList(),
        engine: String = "SQLite",
    ) {
        val formattedSql =
            if (bindArgs.isNotEmpty()) {
                "$sql -- args: [${bindArgs.joinToString(", ")}]"
            } else {
                sql
            }
        DatabaseLogRecorder.record(
            databaseName = databaseName,
            sql = formattedSql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = isSuccess,
            errorMessage = errorMessage,
            affectedRows = affectedRows,
            engine = engine,
        )
    }

    /**
     * Convenience method to log a SELECT query.
     */
    public fun logSelect(
        databaseName: String,
        sql: String,
        durationMs: Long = 0L,
        tableName: String? = null,
        rowCount: Long? = null,
        bindArgs: List<Any?> = emptyList(),
    ) {
        logQuery(
            databaseName = databaseName,
            sql = sql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = true,
            affectedRows = rowCount,
            bindArgs = bindArgs,
        )
    }

    /**
     * Convenience method to log an INSERT operation.
     */
    public fun logInsert(
        databaseName: String,
        sql: String,
        durationMs: Long = 0L,
        tableName: String? = null,
        affectedRows: Long? = 1L,
        bindArgs: List<Any?> = emptyList(),
    ) {
        logQuery(
            databaseName = databaseName,
            sql = sql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = true,
            affectedRows = affectedRows,
            bindArgs = bindArgs,
        )
    }

    /**
     * Convenience method to log an UPDATE operation.
     */
    public fun logUpdate(
        databaseName: String,
        sql: String,
        durationMs: Long = 0L,
        tableName: String? = null,
        affectedRows: Long? = null,
        bindArgs: List<Any?> = emptyList(),
    ) {
        logQuery(
            databaseName = databaseName,
            sql = sql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = true,
            affectedRows = affectedRows,
            bindArgs = bindArgs,
        )
    }

    /**
     * Convenience method to log a DELETE operation.
     */
    public fun logDelete(
        databaseName: String,
        sql: String,
        durationMs: Long = 0L,
        tableName: String? = null,
        affectedRows: Long? = null,
        bindArgs: List<Any?> = emptyList(),
    ) {
        logQuery(
            databaseName = databaseName,
            sql = sql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = true,
            affectedRows = affectedRows,
            bindArgs = bindArgs,
        )
    }

    /**
     * Convenience method to log a failed database query or operation.
     */
    public fun logError(
        databaseName: String,
        sql: String,
        error: Throwable,
        durationMs: Long = 0L,
        tableName: String? = null,
        bindArgs: List<Any?> = emptyList(),
    ) {
        logQuery(
            databaseName = databaseName,
            sql = sql,
            durationMs = durationMs,
            tableName = tableName,
            isSuccess = false,
            errorMessage = error.message ?: error::class.simpleName ?: DEFAULT_ERROR_MESSAGE,
            bindArgs = bindArgs,
        )
    }

    /**
     * Executes a database block while automatically measuring execution duration,
     * capturing any thrown exceptions, and recording the query in AELog Database Logs.
     *
     * Example:
     * ```kotlin
     * val users = AELog.database.trace("app.db", "SELECT * FROM users") {
     *     userDao.getAllUsers()
     * }
     * ```
     */
    public inline fun <T> trace(
        databaseName: String,
        sql: String,
        tableName: String? = null,
        bindArgs: List<Any?> = emptyList(),
        block: () -> T,
    ): T {
        val start =
            kotlin.time.Clock.System
                .now()
                .toEpochMilliseconds()
        try {
            val result = block()
            val duration =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds() - start
            val affected =
                when (result) {
                    is Number -> result.toLong()
                    is Collection<*> -> result.size.toLong()
                    else -> null
                }
            logQuery(
                databaseName = databaseName,
                sql = sql,
                durationMs = duration,
                tableName = tableName,
                isSuccess = true,
                affectedRows = affected,
                bindArgs = bindArgs,
            )
            return result
        } catch (t: Throwable) {
            val duration =
                kotlin.time.Clock.System
                    .now()
                    .toEpochMilliseconds() - start
            logQuery(
                databaseName = databaseName,
                sql = sql,
                durationMs = duration,
                tableName = tableName,
                isSuccess = false,
                errorMessage = t.message ?: t::class.simpleName ?: DEFAULT_ERROR_MESSAGE,
                bindArgs = bindArgs,
            )
            throw t
        }
    }

    /**
     * Creates a [DatabaseQueryInterceptor] that forwards all queries to AELog Database Logs.
     *
     * Can be directly plugged into Android Room's `setQueryCallback`:
     * ```kotlin
     * val interceptor = AELog.database.createQueryInterceptor("app.db")
     * roomBuilder.setQueryCallback(
     *     { sql, bindArgs -> interceptor.onQuery(sql, bindArgs) },
     *     Executors.newSingleThreadExecutor()
     * )
     * ```
     */
    public fun createQueryInterceptor(databaseName: String): DatabaseQueryInterceptor =
        DatabaseQueryInterceptor { sql, bindArgs ->
            logQuery(databaseName = databaseName, sql = sql, durationMs = 0L, bindArgs = bindArgs)
        }

    /**
     * Returns a logger lambda directly compatible with SQLDelight's `LogSqliteDriver`.
     *
     * ### Usage:
     * ```kotlin
     * val driver = LogSqliteDriver(baseDriver, AELog.database.sqlDelightLogger("app.db"))
     * ```
     */
    public fun sqlDelightLogger(databaseName: String = DEFAULT_DB_NAME): (String) -> Unit =
        { sql -> logQuery(databaseName = databaseName, sql = sql) }

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

/**
 * Generic query listener callback for intercepting database queries from framework listeners
 * (such as Android Room's `RoomDatabase.QueryCallback`, SQLDelight drivers, or custom SQLite wrappers).
 */
public fun interface DatabaseQueryInterceptor {
    public fun onQuery(
        sql: String,
        bindArgs: List<Any?>,
    )
}
