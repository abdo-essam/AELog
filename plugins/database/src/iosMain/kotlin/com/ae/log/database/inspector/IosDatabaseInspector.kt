@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ae.log.database.inspector

import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.refTo
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSString
import platform.Foundation.NSUserDomainMask
import platform.Foundation.stringByAppendingPathComponent
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread

@OptIn(BetaInteropApi::class)
internal class IosDatabaseInspector(
    private val config: DatabasePluginConfig,
) : DatabaseInspector {
    private val fileManager = NSFileManager.defaultManager
    private val registeredDatabases = mutableListOf<DbInfo>()

    override fun registerDatabase(dbInfo: DbInfo) {
        if (registeredDatabases.none { it.path == dbInfo.path }) {
            registeredDatabases.add(dbInfo)
        }
    }

    override fun listDatabases(): List<DbInfo> {
        val result = mutableListOf<DbInfo>()
        val searchDomains =
            listOf(
                NSApplicationSupportDirectory,
                NSDocumentDirectory,
                NSLibraryDirectory,
            )

        searchDomains.forEach { domain ->
            val dirUrl =
                fileManager.URLForDirectory(
                    directory = domain,
                    inDomain = NSUserDomainMask,
                    appropriateForURL = null,
                    create = false,
                    error = null,
                ) ?: return@forEach

            val basePath = dirUrl.path ?: return@forEach
            scanDirectory(basePath, result)
        }

        config.additionalSearchPaths.forEach { extraPath ->
            scanDirectory(extraPath, result)
        }

        registeredDatabases.forEach { reg ->
            if (result.none { it.path == reg.path }) {
                result.add(reg)
            }
        }

        return result
    }

    private fun scanDirectory(
        basePath: String,
        result: MutableList<DbInfo>,
    ) {
        if (!fileManager.fileExistsAtPath(basePath)) return

        val enumerator = fileManager.enumeratorAtPath(basePath) ?: return
        while (true) {
            val relativePath = enumerator.nextObject() as? String ?: break
            if (isSqliteFilename(relativePath) && !isAuxiliaryFile(relativePath)) {
                @Suppress("CAST_NEVER_SUCCEEDS")
                val fullPath = (basePath as NSString).stringByAppendingPathComponent(relativePath)
                if (result.none { it.path == fullPath }) {
                    val name = relativePath.substringAfterLast("/")
                    val isEncrypted = isEncryptedFile(fullPath)
                    result.add(
                        DbInfo(
                            name = name,
                            path = fullPath,
                            isEncrypted = isEncrypted,
                        ),
                    )
                }
            }
        }
    }

    private val virtualTables = mutableMapOf<String, List<DbTable>>()

    public fun registerVirtualTable(
        dbName: String,
        table: DbTable,
    ) {
        val current = virtualTables[dbName]?.toMutableList() ?: mutableListOf()
        current.removeAll { it.name == table.name }
        current.add(table)
        virtualTables[dbName] = current
    }

    override fun listTables(dbInfo: DbInfo): List<DbTable> {
        val virtual = virtualTables[dbInfo.name]
        if (!virtual.isNullOrEmpty()) {
            return virtual
        }

        if (dbInfo.name == "ios_sample.db") {
            return listOf(
                DbTable(name = "users", rowCount = 4L, columns = emptyList(), isSystemTable = false),
                DbTable(name = "products", rowCount = 4L, columns = emptyList(), isSystemTable = false),
                DbTable(name = "orders", rowCount = 3L, columns = emptyList(), isSystemTable = false),
            )
        }

        // Safe query against sqlite_master on iOS
        val queryResult =
            query(
                dbInfo = dbInfo,
                sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name",
                allowWrite = false,
            )

        if (!queryResult.isSuccess || queryResult.rows.isEmpty()) {
            return emptyList()
        }

        return queryResult.rows.mapNotNull { row ->
            val name = row.firstOrNull() ?: return@mapNotNull null
            val isSystem = name.startsWith("sqlite_")
            DbTable(
                name = name,
                rowCount = -1L,
                columns = emptyList(),
                isSystemTable = isSystem,
            )
        }
    }

    override fun query(
        dbInfo: DbInfo,
        sql: String,
        args: List<String>,
        allowWrite: Boolean,
    ): QueryResult {
        try {
            validateSqlSafety(sql, allowWrite)
        } catch (e: IllegalArgumentException) {
            val err = QueryResult.error(e.message ?: "Write operation disallowed")
            if (!sql.trimStart().uppercase().startsWith("PRAGMA")) {
                com.ae.log.database.DatabaseLogRecorder.record(
                    databaseName = dbInfo.name,
                    sql = sql,
                    durationMs = 0L,
                    isSuccess = false,
                    errorMessage = err.errorMessage,
                )
            }
            return err
        }

        if (dbInfo.name == "ios_sample.db") {
            val clean = sql.trim().uppercase()
            val queryResult = when {
                clean.startsWith("PRAGMA TABLE_INFO(\"USERS\")") || clean.startsWith("PRAGMA TABLE_INFO('USERS')") || clean.contains("TABLE_INFO(\"USERS\")") ->
                    QueryResult.success(
                        columns = listOf("cid", "name", "type", "notnull", "dflt_value", "pk"),
                        rows = listOf(
                            listOf("0", "id", "INTEGER", "1", null, "1"),
                            listOf("1", "name", "TEXT", "1", null, "0"),
                            listOf("2", "email", "TEXT", "1", null, "0"),
                            listOf("3", "role", "TEXT", "1", null, "0"),
                        ),
                        durationMs = 1L,
                    )

                clean.startsWith("PRAGMA TABLE_INFO(\"PRODUCTS\")") || clean.startsWith("PRAGMA TABLE_INFO('PRODUCTS')") || clean.contains("TABLE_INFO(\"PRODUCTS\")") ->
                    QueryResult.success(
                        columns = listOf("cid", "name", "type", "notnull", "dflt_value", "pk"),
                        rows = listOf(
                            listOf("0", "id", "INTEGER", "1", null, "1"),
                            listOf("1", "title", "TEXT", "1", null, "0"),
                            listOf("2", "price", "REAL", "1", null, "0"),
                            listOf("3", "stock", "INTEGER", "1", null, "0"),
                            listOf("4", "category", "TEXT", "1", null, "0"),
                        ),
                        durationMs = 1L,
                    )

                clean.startsWith("PRAGMA TABLE_INFO(\"ORDERS\")") || clean.startsWith("PRAGMA TABLE_INFO('ORDERS')") || clean.contains("TABLE_INFO(\"ORDERS\")") ->
                    QueryResult.success(
                        columns = listOf("cid", "name", "type", "notnull", "dflt_value", "pk"),
                        rows = listOf(
                            listOf("0", "id", "INTEGER", "1", null, "1"),
                            listOf("1", "user_id", "INTEGER", "1", null, "0"),
                            listOf("2", "total", "REAL", "1", null, "0"),
                            listOf("3", "status", "TEXT", "1", null, "0"),
                        ),
                        durationMs = 1L,
                    )

                clean.startsWith("PRAGMA INDEX_LIST") ->
                    QueryResult.success(
                        columns = listOf("seq", "name", "unique", "origin", "partial"),
                        rows = emptyList(),
                        durationMs = 1L,
                    )

                clean.contains("FROM \"USERS\"") || clean.contains("FROM USERS") ->
                    QueryResult.success(
                        columns = listOf("id", "name", "email", "role"),
                        rows = listOf(
                            listOf("1", "Alice Smith", "alice@example.com", "Admin"),
                            listOf("2", "Bob Jones", "bob@example.com", "Developer"),
                            listOf("3", "Charlie Brown", "charlie@example.com", "Designer"),
                            listOf("4", "Diana Prince", "diana@example.com", "Manager"),
                        ),
                        durationMs = 2L,
                    )

                clean.contains("FROM \"PRODUCTS\"") || clean.contains("FROM PRODUCTS") ->
                    QueryResult.success(
                        columns = listOf("id", "title", "price", "stock", "category"),
                        rows = listOf(
                            listOf("1", "MacBook Pro 16\"", "2499.00", "15", "Hardware"),
                            listOf("2", "Ergonomic Mouse", "59.99", "45", "Accessories"),
                            listOf("3", "Mechanical Keyboard", "129.50", "20", "Accessories"),
                            listOf("4", "4K UltraSharp Display", "599.00", "8", "Monitors"),
                        ),
                        durationMs = 2L,
                    )

                clean.contains("FROM \"ORDERS\"") || clean.contains("FROM ORDERS") ->
                    QueryResult.success(
                        columns = listOf("id", "user_id", "total", "status"),
                        rows = listOf(
                            listOf("1", "1", "2558.99", "Delivered"),
                            listOf("2", "2", "129.50", "Shipped"),
                            listOf("3", "3", "59.99", "Processing"),
                        ),
                        durationMs = 2L,
                    )

                isWriteStatement(sql) ->
                    QueryResult.writeSuccess(affectedRows = 1L, durationMs = 3L)

                else ->
                    QueryResult.success(
                        columns = listOf("result"),
                        rows = listOf(listOf("OK")),
                        durationMs = 1L,
                    )
            }

            if (!sql.trimStart().uppercase().startsWith("PRAGMA")) {
                com.ae.log.database.DatabaseLogRecorder.record(
                    databaseName = dbInfo.name,
                    sql = sql,
                    durationMs = queryResult.executionDurationMs,
                    isSuccess = queryResult.isSuccess,
                    errorMessage = queryResult.errorMessage,
                    affectedRows = queryResult.affectedRows,
                )
            }

            return queryResult
        }

        // Check if file exists in the iOS sandbox
        if (!fileManager.fileExistsAtPath(dbInfo.path)) {
            val err = QueryResult.error("Database file not found: ${dbInfo.path}")
            if (!sql.trimStart().uppercase().startsWith("PRAGMA")) {
                com.ae.log.database.DatabaseLogRecorder.record(
                    databaseName = dbInfo.name,
                    sql = sql,
                    durationMs = 0L,
                    isSuccess = false,
                    errorMessage = err.errorMessage,
                )
            }
            return err
        }

        if (dbInfo.isEncrypted && config.passphraseProvider?.getPassphrase(dbInfo.name) == null) {
            val err = QueryResult.error("Database is encrypted. Please configure a PassphraseProvider.")
            if (!sql.trimStart().uppercase().startsWith("PRAGMA")) {
                com.ae.log.database.DatabaseLogRecorder.record(
                    databaseName = dbInfo.name,
                    sql = sql,
                    durationMs = 0L,
                    isSuccess = false,
                    errorMessage = err.errorMessage,
                )
            }
            return err
        }

        val err = QueryResult.error(
            "SQLite runtime query engine is available on Android / JVM. On iOS, connect a custom DatabaseInspector or registered snapshot.",
        )
        if (!sql.trimStart().uppercase().startsWith("PRAGMA")) {
            com.ae.log.database.DatabaseLogRecorder.record(
                databaseName = dbInfo.name,
                sql = sql,
                durationMs = 0L,
                isSuccess = false,
                errorMessage = err.errorMessage,
            )
        }
        return err
    }

    private fun isSqliteFilename(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".sqlite") || lower.endsWith(".db") || lower.endsWith(".sqlite3")
    }

    private fun isAuxiliaryFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith("-wal") || lower.endsWith("-shm") || lower.endsWith("-journal")
    }

    private fun isEncryptedFile(path: String): Boolean {
        val file = fopen(path, "rb") ?: return false
        val header = ByteArray(16)
        val read = fread(header.refTo(0), 1u, 16u, file)
        fclose(file)
        if (read < 16u) return false
        val prefix = header.take(15).map { it.toInt().toChar() }.joinToString("")
        return !prefix.startsWith("SQLite format 3")
    }
}

internal actual fun createPlatformDatabaseInspector(config: DatabasePluginConfig): DatabaseInspector =
    IosDatabaseInspector(config)
