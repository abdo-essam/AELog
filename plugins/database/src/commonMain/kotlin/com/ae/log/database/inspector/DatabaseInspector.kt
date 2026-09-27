package com.ae.log.database.inspector

import com.ae.log.database.model.DatabaseOperation
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import com.ae.log.database.model.TableSchema

private const val OP_SELECT = "SELECT"
private const val OP_INSERT = "INSERT"
private const val OP_UPDATE = "UPDATE"
private const val OP_DELETE = "DELETE"
private const val OP_DROP = "DROP"
private const val OP_CREATE = "CREATE"
private const val OP_ALTER = "ALTER"
private const val OP_REPLACE = "REPLACE"
private const val OP_PRAGMA = "PRAGMA"

/**
 * Common abstraction for inspecting SQLite databases across platforms.
 */
public interface DatabaseInspector {
    /**
     * Lists all accessible databases (auto-discovered on the device/filesystem or manually registered).
     */
    public fun listDatabases(): List<DbInfo>

    /**
     * Fast-path or cached lookup for a specific database by name.
     */
    public fun getDatabase(name: String): DbInfo? = listDatabases().firstOrNull { it.name == name }

    /**
     * Retrieves the tables and column schemas for the specified database.
     */
    public fun listTables(dbInfo: DbInfo): List<DbTable>

    /**
     * Executes a SQL statement against the target database.
     *
     * @param dbInfo The database to execute against.
     * @param sql The SQL statement.
     * @param args Bind arguments (optional).
     * @param allowWrite Whether write statements are permitted.
     * @param recordLog Whether to record this query execution in DatabaseLogRecorder.
     */
    public fun query(
        dbInfo: DbInfo,
        sql: String,
        args: List<String> = emptyList(),
        allowWrite: Boolean = false,
        recordLog: Boolean = true,
    ): QueryResult

    /**
     * Retrieves the detailed schema (columns with types and constraints, and indexes) for a table.
     */
    public fun getSchema(
        dbInfo: DbInfo,
        tableName: String,
    ): TableSchema =
        TableSchema(
            tableName = tableName,
            columns = emptyList(),
            indexes = emptyList(),
            foreignKeys = emptyList(),
        )

    /**
     * Retrieves paginated rows from a specific table with optional column sorting and search filter.
     */
    public fun getTableData(
        dbInfo: DbInfo,
        tableName: String,
        offset: Int = 0,
        limit: Int = 50,
        sortColumn: String? = null,
        sortAscending: Boolean = true,
        searchQuery: String? = null,
    ): QueryResult {
        val safeLimit = limit.coerceIn(1, 1000)
        val safeOffset = offset.coerceAtLeast(0)
        val escapedTable = "\"" + tableName.replace("\"", "\"\"") + "\""
        return query(
            dbInfo = dbInfo,
            sql = "SELECT * FROM $escapedTable LIMIT $safeLimit OFFSET $safeOffset",
            allowWrite = false,
            recordLog = false,
        )
    }

    /**
     * Registers a database explicitly with this inspector.
     */
    public fun registerDatabase(dbInfo: DbInfo) {}
}

/**
 * Checks if the given SQL statement is an operation that modifies data or schema.
 */
public fun isWriteStatement(sql: String): Boolean {
    val cleanSql = sql.trimStart().uppercase()
    val writeKeywords =
        listOf(OP_INSERT, OP_UPDATE, OP_DELETE, OP_DROP, OP_CREATE, OP_ALTER, OP_REPLACE, "TRUNCATE", "VACUUM")
    return writeKeywords.any { cleanSql.startsWith(it) }
}

/**
 * Detects the general operation keyword of the given SQL query.
 */
public fun detectOperation(sql: String): DatabaseOperation {
    val clean = sql.trimStart().uppercase()
    return when {
        clean.startsWith(OP_SELECT) ||
            clean.startsWith("WITH") ||
            clean.startsWith("EXPLAIN") -> DatabaseOperation.SELECT
        clean.startsWith(OP_INSERT) -> DatabaseOperation.INSERT
        clean.startsWith(OP_REPLACE) -> DatabaseOperation.REPLACE
        clean.startsWith(OP_UPDATE) -> DatabaseOperation.UPDATE
        clean.startsWith(OP_DELETE) -> DatabaseOperation.DELETE
        clean.startsWith(OP_CREATE) -> DatabaseOperation.CREATE
        clean.startsWith(OP_DROP) -> DatabaseOperation.DROP
        clean.startsWith(OP_ALTER) -> DatabaseOperation.ALTER
        clean.startsWith(OP_PRAGMA) -> DatabaseOperation.PRAGMA
        clean.startsWith("BEGIN") ||
            clean.startsWith("COMMIT") ||
            clean.startsWith("ROLLBACK") ||
            clean.startsWith("SAVEPOINT") ||
            clean.startsWith("RELEASE") -> DatabaseOperation.TRANSACTION
        else -> DatabaseOperation.OTHER
    }
}

/**
 * Validates whether the given SQL statement is permitted under the current write settings.
 *
 * @throws IllegalArgumentException if the statement is a write operation and [allowWrite] is `false`.
 */
public fun validateSqlSafety(
    sql: String,
    allowWrite: Boolean,
) {
    require(allowWrite || !isWriteStatement(sql)) {
        "Write operations (INSERT, UPDATE, DELETE, etc.) are disabled. " +
            "To enable them, configure DatabasePluginConfig(allowWrite = true) or enable Edit Mode."
    }
}
