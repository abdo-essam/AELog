package com.ae.log.database.inspector

import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.QueryResult
import com.ae.log.database.model.TableColumn
import com.ae.log.database.model.TableForeignKey
import com.ae.log.database.model.TableIndex
import com.ae.log.database.model.TableSchema
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

private const val COLUMN_NAME = "name"

/**
 * Base implementation of [DatabaseInspector] providing thread-safe database registration,
 * schema inspection, and SQL injection protected table data queries.
 */
public abstract class BaseDatabaseInspector : DatabaseInspector {
    private val lock = SynchronizedObject()
    private val registeredDatabasesList = mutableListOf<DbInfo>()

    /**
     * Returns an immutable snapshot of all registered databases.
     */
    protected val registeredDatabases: List<DbInfo>
        get() = synchronized(lock) { registeredDatabasesList.toList() }

    override fun registerDatabase(dbInfo: DbInfo) {
        synchronized(lock) {
            if (registeredDatabasesList.none { it.path == dbInfo.path }) {
                registeredDatabasesList.add(dbInfo)
            }
        }
    }

    override fun getDatabase(name: String): DbInfo? {
        val registered = synchronized(lock) { registeredDatabasesList.firstOrNull { it.name == name } }
        if (registered != null) return registered
        return listDatabases().firstOrNull { it.name == name }
    }

    override fun getSchema(
        dbInfo: DbInfo,
        tableName: String,
    ): TableSchema =
        TableSchema(
            tableName = tableName,
            columns = fetchColumns(dbInfo, tableName),
            indexes = fetchIndexes(dbInfo, tableName),
            foreignKeys = fetchForeignKeys(dbInfo, tableName),
        )

    protected open fun fetchColumns(
        dbInfo: DbInfo,
        tableName: String,
    ): List<TableColumn> {
        val escapedTable = tableName.replace("\"", "\"\"")
        val colResult = query(dbInfo, "PRAGMA table_info(\"$escapedTable\")", allowWrite = false)
        if (!colResult.isSuccess) return emptyList()

        val nameIdx = colResult.columns.indexOf(COLUMN_NAME)
        val typeIdx = colResult.columns.indexOf("type")
        val notNullIdx = colResult.columns.indexOf("notnull")
        val dfltIdx = colResult.columns.indexOf("dflt_value")
        val pkIdx = colResult.columns.indexOf("pk")

        if (nameIdx < 0) return emptyList()

        return colResult.rows.map { row ->
            TableColumn(
                name = row.getOrNull(nameIdx) ?: "",
                type = if (typeIdx >= 0) row.getOrNull(typeIdx) ?: "TEXT" else "TEXT",
                isPrimaryKey = if (pkIdx >= 0) (row.getOrNull(pkIdx)?.toIntOrNull() ?: 0) > 0 else false,
                isNotNull = if (notNullIdx >= 0) row.getOrNull(notNullIdx) == "1" else false,
                defaultValue = if (dfltIdx >= 0) row.getOrNull(dfltIdx) else null,
            )
        }
    }

    protected open fun fetchIndexes(
        dbInfo: DbInfo,
        tableName: String,
    ): List<TableIndex> {
        val escapedTable = tableName.replace("\"", "\"\"")
        val indexResult = query(dbInfo, "PRAGMA index_list(\"$escapedTable\")", allowWrite = false)
        if (!indexResult.isSuccess) return emptyList()

        val nameIdx = indexResult.columns.indexOf(COLUMN_NAME)
        val uniqueIdx = indexResult.columns.indexOf("unique")
        if (nameIdx < 0) return emptyList()

        return indexResult.rows.mapNotNull { row ->
            val idxName = row.getOrNull(nameIdx) ?: return@mapNotNull null
            val isUnique = if (uniqueIdx >= 0) row.getOrNull(uniqueIdx) == "1" else false

            val escapedIdx = idxName.replace("\"", "\"\"")
            val idxInfoResult = query(dbInfo, "PRAGMA index_info(\"$escapedIdx\")", allowWrite = false)
            val colNameIdx = idxInfoResult.columns.indexOf(COLUMN_NAME)
            val indexCols =
                if (idxInfoResult.isSuccess && colNameIdx >= 0) {
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
    }

    protected open fun fetchForeignKeys(
        dbInfo: DbInfo,
        tableName: String,
    ): List<TableForeignKey> {
        val escapedTable = tableName.replace("\"", "\"\"")
        val fkResult = query(dbInfo, "PRAGMA foreign_key_list(\"$escapedTable\")", allowWrite = false)
        if (!fkResult.isSuccess) return emptyList()

        val idIdx = fkResult.columns.indexOf("id")
        val tableIdx = fkResult.columns.indexOf("table")
        val fromIdx = fkResult.columns.indexOf("from")
        val toIdx = fkResult.columns.indexOf("to")
        val onUpdateIdx = fkResult.columns.indexOf("on_update")
        val onDeleteIdx = fkResult.columns.indexOf("on_delete")

        if (tableIdx < 0 || fromIdx < 0) return emptyList()

        return fkResult.rows.mapNotNull { row ->
            val fromCol = row.getOrNull(fromIdx) ?: return@mapNotNull null
            val targetTab = row.getOrNull(tableIdx) ?: return@mapNotNull null
            val targetCol = if (toIdx >= 0) row.getOrNull(toIdx) ?: "" else ""
            TableForeignKey(
                id = if (idIdx >= 0) row.getOrNull(idIdx)?.toIntOrNull() ?: 0 else 0,
                fromColumn = fromCol,
                targetTable = targetTab,
                targetColumn = targetCol,
                onUpdate = if (onUpdateIdx >= 0) row.getOrNull(onUpdateIdx) ?: "NO ACTION" else "NO ACTION",
                onDelete = if (onDeleteIdx >= 0) row.getOrNull(onDeleteIdx) ?: "NO ACTION" else "NO ACTION",
            )
        }
    }

    /**
     * Retrieves paginated rows with SQL injection protection:
     * - Table names and column names are quoted and verified against schema.
     * - sortColumn is validated against an allowlist of actual schema column names.
     * - searchQuery is properly escaped for LIKE patterns with ESCAPE clause.
     */
    override fun getTableData(
        dbInfo: DbInfo,
        tableName: String,
        offset: Int,
        limit: Int,
        sortColumn: String?,
        sortAscending: Boolean,
        searchQuery: String?,
    ): QueryResult {
        val escapedTable = "\"" + tableName.replace("\"", "\"\"") + "\""
        val schema = getSchema(dbInfo, tableName)
        val validColumns = schema.columns.map { it.name }.toSet()

        val whereClause =
            if (!searchQuery.isNullOrBlank() && validColumns.isNotEmpty()) {
                val safeSearch =
                    searchQuery
                        .replace("\\", "\\\\")
                        .replace("%", "\\%")
                        .replace("_", "\\_")
                        .replace("'", "''")

                val conditions =
                    validColumns.joinToString(" OR ") { col ->
                        val escapedCol = "\"" + col.replace("\"", "\"\"") + "\""
                        "$escapedCol LIKE '%$safeSearch%' ESCAPE '\\'"
                    }
                " WHERE $conditions"
            } else {
                ""
            }

        val orderClause =
            if (!sortColumn.isNullOrBlank() && validColumns.contains(sortColumn)) {
                val dir = if (sortAscending) "ASC" else "DESC"
                val escapedCol = "\"" + sortColumn.replace("\"", "\"\"") + "\""
                " ORDER BY $escapedCol $dir"
            } else {
                ""
            }

        val safeLimit = limit.coerceIn(1, 1000)
        val safeOffset = offset.coerceAtLeast(0)

        return query(
            dbInfo = dbInfo,
            sql = "SELECT * FROM $escapedTable$whereClause$orderClause LIMIT $safeLimit OFFSET $safeOffset",
            allowWrite = false,
            recordLog = false,
        )
    }
}
