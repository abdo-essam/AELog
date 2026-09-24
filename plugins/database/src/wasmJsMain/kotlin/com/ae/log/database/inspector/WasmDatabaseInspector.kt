package com.ae.log.database.inspector

import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult

internal class WasmDatabaseInspector(
    private val config: DatabasePluginConfig,
) : DatabaseInspector {
    private val registeredDatabases = mutableListOf<DbInfo>()
    private val virtualTables = mutableMapOf<String, List<DbTable>>()

    override fun registerDatabase(dbInfo: DbInfo) {
        if (registeredDatabases.none { it.path == dbInfo.path }) {
            registeredDatabases.add(dbInfo)
        }
    }

    public fun registerVirtualTable(
        dbName: String,
        table: DbTable,
    ) {
        val current = virtualTables[dbName]?.toMutableList() ?: mutableListOf()
        current.removeAll { it.name == table.name }
        current.add(table)
        virtualTables[dbName] = current
    }

    override fun listDatabases(): List<DbInfo> = registeredDatabases.toList()

    override fun listTables(dbInfo: DbInfo): List<DbTable> = virtualTables[dbInfo.name] ?: emptyList()

    override fun query(
        dbInfo: DbInfo,
        sql: String,
        args: List<String>,
        allowWrite: Boolean,
    ): QueryResult {
        try {
            validateSqlSafety(sql, allowWrite)
        } catch (e: IllegalArgumentException) {
            return QueryResult.error(e.message ?: "Write operation disallowed")
        }

        return QueryResult.error(
            "Browser environment does not have direct SQLite filesystem access. " +
                "Register virtual tables via DatabaseProxy or supply a custom DatabaseInspector.",
        )
    }
}

internal actual fun createPlatformDatabaseInspector(config: DatabasePluginConfig): DatabaseInspector =
    WasmDatabaseInspector(config)
