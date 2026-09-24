package com.ae.log.database.inspector

import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import com.ae.log.database.model.TableColumn
import com.ae.log.database.model.TableIndex
import com.ae.log.database.model.TableSchema

/**
 * Common abstraction for inspecting SQLite databases across platforms.
 */
public interface DatabaseInspector {
    /**
     * Lists all accessible databases (auto-discovered on the device/filesystem or manually registered).
     */
    public fun listDatabases(): List<DbInfo>

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
     */
    public fun query(
        dbInfo: DbInfo,
        sql: String,
        args: List<String> = emptyList(),
        allowWrite: Boolean = false,
    ): QueryResult

    /**
     * Retrieves the detailed schema (columns with types and constraints, and indexes) for a table.
     */
    public fun getSchema(
        dbInfo: DbInfo,
        tableName: String,
    ): TableSchema {
        val colResult = query(dbInfo, "PRAGMA table_info(\"$tableName\")", allowWrite = false)
        val columns = if (colResult.isSuccess) {
            val nameIdx = colResult.columns.indexOf("name").takeIf { it >= 0 } ?: 1
            val typeIdx = colResult.columns.indexOf("type").takeIf { it >= 0 } ?: 2
            val notNullIdx = colResult.columns.indexOf("notnull").takeIf { it >= 0 } ?: 3
            val dfltIdx = colResult.columns.indexOf("dflt_value").takeIf { it >= 0 } ?: 4
            val pkIdx = colResult.columns.indexOf("pk").takeIf { it >= 0 } ?: 5

            colResult.rows.map { row ->
                TableColumn(
                    name = row.getOrNull(nameIdx) ?: "",
                    type = row.getOrNull(typeIdx) ?: "TEXT",
                    isPrimaryKey = (row.getOrNull(pkIdx)?.toIntOrNull() ?: 0) > 0,
                    isNotNull = row.getOrNull(notNullIdx) == "1",
                    defaultValue = row.getOrNull(dfltIdx),
                )
            }
        } else {
            emptyList()
        }

        val indexResult = query(dbInfo, "PRAGMA index_list(\"$tableName\")", allowWrite = false)
        val indexes = if (indexResult.isSuccess) {
            val nameIdx = indexResult.columns.indexOf("name").takeIf { it >= 0 } ?: 1
            val uniqueIdx = indexResult.columns.indexOf("unique").takeIf { it >= 0 } ?: 2

            indexResult.rows.mapNotNull { row ->
                val idxName = row.getOrNull(nameIdx) ?: return@mapNotNull null
                val isUnique = row.getOrNull(uniqueIdx) == "1"

                val idxInfoResult = query(dbInfo, "PRAGMA index_info(\"$idxName\")", allowWrite = false)
                val colNameIdx = idxInfoResult.columns.indexOf("name").takeIf { it >= 0 } ?: 2
                val indexCols = if (idxInfoResult.isSuccess) {
                    idxInfoResult.rows.mapNotNull { it.getOrNull(colNameIdx) }
                } else {
                    emptyList()
                }

                TableIndex(
                    name = idxName,
                    isUnique = isUnique,
                    columns = indexCols,
                )
            }
        } else {
            emptyList()
        }

        return TableSchema(
            tableName = tableName,
            columns = columns,
            indexes = indexes,
        )
    }

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
        val escapedTable = "\"$tableName\""
        val whereClause = if (!searchQuery.isNullOrBlank()) {
            val safeSearch = searchQuery.replace("'", "''")
            val schema = getSchema(dbInfo, tableName)
            val textCols = schema.columns.map { it.name }
            if (textCols.isNotEmpty()) {
                val conditions = textCols.joinToString(" OR ") { col ->
                    "\"$col\" LIKE '%$safeSearch%'"
                }
                " WHERE $conditions"
            } else {
                ""
            }
        } else {
            ""
        }

        val orderClause = if (!sortColumn.isNullOrBlank()) {
            val dir = if (sortAscending) "ASC" else "DESC"
            " ORDER BY \"$sortColumn\" $dir"
        } else {
            ""
        }

        return query(
            dbInfo = dbInfo,
            sql = "SELECT * FROM $escapedTable$whereClause$orderClause LIMIT $limit OFFSET $offset",
            allowWrite = false,
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
    val writeKeywords = listOf("INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER", "REPLACE", "TRUNCATE", "VACUUM")
    return writeKeywords.any { cleanSql.startsWith(it) }
}

/**
 * Detects the general operation keyword of the given SQL query.
 */
public fun detectOperation(sql: String): String {
    val clean = sql.trimStart().uppercase()
    return when {
        clean.startsWith("SELECT") -> "SELECT"
        clean.startsWith("INSERT") -> "INSERT"
        clean.startsWith("UPDATE") -> "UPDATE"
        clean.startsWith("DELETE") -> "DELETE"
        clean.startsWith("CREATE") -> "CREATE"
        clean.startsWith("DROP") -> "DROP"
        clean.startsWith("ALTER") -> "ALTER"
        clean.startsWith("PRAGMA") -> "PRAGMA"
        else -> "OTHER"
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
    if (!allowWrite && isWriteStatement(sql)) {
        throw IllegalArgumentException(
            "Write operations (INSERT, UPDATE, DELETE, etc.) are disabled. " +
                "To enable them, configure DatabasePluginConfig(allowWrite = true) or enable Edit Mode.",
        )
    }
}
