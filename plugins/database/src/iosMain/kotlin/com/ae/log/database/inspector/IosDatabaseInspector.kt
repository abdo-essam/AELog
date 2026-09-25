@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
@file:Suppress("ktlint:standard:max-line-length")

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

private const val SAMPLE_DB_NAME = "ios_sample.db"
private const val PRAGMA_KEYWORD = "PRAGMA"
private const val SQLITE_HEADER_PREFIX = "SQLite format 3"
private const val TYPE_INTEGER = "INTEGER"
private const val TYPE_TEXT = "TEXT"
private const val TYPE_REAL = "REAL"
private const val COL_ID = "id"
private const val COL_NAME = "name"
private const val COL_EMAIL = "email"
private const val COL_ROLE = "role"
private const val COL_TITLE = "title"
private const val COL_PRICE = "price"
private const val COL_STOCK = "stock"
private const val COL_CATEGORY = "category"
private const val COL_USER_ID = "user_id"
private const val COL_TOTAL = "total"
private const val COL_STATUS = "status"
private const val CATEGORY_ACCESSORIES = "Accessories"
private const val SAMPLE_PRICE_MOUSE = "59.99"
private const val SAMPLE_PRICE_KEYBOARD = "129.50"

private val TABLE_INFO_COLUMNS = listOf("cid", COL_NAME, "type", "notnull", "dflt_value", "pk")
private val USERS_COLUMNS = listOf(COL_ID, COL_NAME, COL_EMAIL, COL_ROLE)
private val PRODUCTS_COLUMNS = listOf(COL_ID, COL_TITLE, COL_PRICE, COL_STOCK, COL_CATEGORY)
private val ORDERS_COLUMNS = listOf(COL_ID, COL_USER_ID, COL_TOTAL, COL_STATUS)

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

        if (dbInfo.name == SAMPLE_DB_NAME) {
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
            if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
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

        if (dbInfo.name == SAMPLE_DB_NAME) {
            val clean = sql.trim().uppercase()
            val queryResult =
                when {
                    clean.contains("TABLE_INFO") && clean.contains("USERS") ->
                        QueryResult.success(
                            columns = TABLE_INFO_COLUMNS,
                            rows =
                                listOf(
                                    listOf("0", COL_ID, TYPE_INTEGER, "1", null, "1"),
                                    listOf("1", COL_NAME, TYPE_TEXT, "1", null, "0"),
                                    listOf("2", COL_EMAIL, TYPE_TEXT, "1", null, "0"),
                                    listOf("3", COL_ROLE, TYPE_TEXT, "1", null, "0"),
                                ),
                            durationMs = 1L,
                        )

                    clean.contains("TABLE_INFO") && clean.contains("PRODUCTS") ->
                        QueryResult.success(
                            columns = TABLE_INFO_COLUMNS,
                            rows =
                                listOf(
                                    listOf("0", COL_ID, TYPE_INTEGER, "1", null, "1"),
                                    listOf("1", COL_TITLE, TYPE_TEXT, "1", null, "0"),
                                    listOf("2", COL_PRICE, TYPE_REAL, "1", null, "0"),
                                    listOf("3", COL_STOCK, TYPE_INTEGER, "1", null, "0"),
                                    listOf("4", COL_CATEGORY, TYPE_TEXT, "1", null, "0"),
                                ),
                            durationMs = 1L,
                        )

                    clean.contains("TABLE_INFO") && clean.contains("ORDERS") ->
                        QueryResult.success(
                            columns = TABLE_INFO_COLUMNS,
                            rows =
                                listOf(
                                    listOf("0", COL_ID, TYPE_INTEGER, "1", null, "1"),
                                    listOf("1", COL_USER_ID, TYPE_INTEGER, "1", null, "0"),
                                    listOf("2", COL_TOTAL, TYPE_REAL, "1", null, "0"),
                                    listOf("3", COL_STATUS, TYPE_TEXT, "1", null, "0"),
                                ),
                            durationMs = 1L,
                        )

                    clean.startsWith("PRAGMA INDEX_LIST") ->
                        QueryResult.success(
                            columns = listOf("seq", COL_NAME, "unique", "origin", "partial"),
                            rows = emptyList(),
                            durationMs = 1L,
                        )

                    clean.contains("FROM \"USERS\"") || clean.contains("FROM USERS") ->
                        QueryResult.success(
                            columns = USERS_COLUMNS,
                            rows =
                                listOf(
                                    listOf("1", "Alice Smith", "alice@example.com", "Admin"),
                                    listOf("2", "Bob Jones", "bob@example.com", "Developer"),
                                    listOf("3", "Charlie Brown", "charlie@example.com", "Designer"),
                                    listOf("4", "Diana Prince", "diana@example.com", "Manager"),
                                ),
                            durationMs = 2L,
                        )

                    clean.contains("FROM \"PRODUCTS\"") || clean.contains("FROM PRODUCTS") ->
                        QueryResult.success(
                            columns = PRODUCTS_COLUMNS,
                            rows =
                                listOf(
                                    listOf("1", "MacBook Pro 16\"", "2499.00", "15", "Hardware"),
                                    listOf("2", "Ergonomic Mouse", SAMPLE_PRICE_MOUSE, "45", CATEGORY_ACCESSORIES),
                                    listOf(
                                        "3",
                                        "Mechanical Keyboard",
                                        SAMPLE_PRICE_KEYBOARD,
                                        "20",
                                        CATEGORY_ACCESSORIES,
                                    ),
                                    listOf("4", "4K UltraSharp Display", "599.00", "8", "Monitors"),
                                ),
                            durationMs = 2L,
                        )

                    clean.contains("FROM \"ORDERS\"") || clean.contains("FROM ORDERS") ->
                        QueryResult.success(
                            columns = ORDERS_COLUMNS,
                            rows =
                                listOf(
                                    listOf("1", "1", "2558.99", "Delivered"),
                                    listOf("2", "2", SAMPLE_PRICE_KEYBOARD, "Shipped"),
                                    listOf("3", "3", SAMPLE_PRICE_MOUSE, "Processing"),
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

            if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
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
            if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
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
            if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
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

        val err =
            QueryResult.error(
                "SQLite runtime query engine is available on Android / JVM. " +
                    "On iOS, connect a custom DatabaseInspector or registered snapshot.",
            )
        if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
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
        return lower.endsWith("-wal") ||
            lower.endsWith(".wal") ||
            lower.endsWith("-shm") ||
            lower.endsWith(".shm") ||
            lower.endsWith("-journal") ||
            lower.endsWith(".journal") ||
            lower.endsWith("-lck") ||
            lower.endsWith(".lck") ||
            lower.endsWith("-lock") ||
            lower.endsWith(".lock") ||
            lower.endsWith("-tmp") ||
            lower.endsWith(".tmp") ||
            lower.endsWith("-bak") ||
            lower.endsWith(".bak")
    }

    private fun isEncryptedFile(path: String): Boolean {
        val file = fopen(path, "rb") ?: return false
        val header = ByteArray(16)
        val read = fread(header.refTo(0), 1u, 16u, file)
        fclose(file)
        if (read < 16u) return false
        val prefix = header.take(15).map { it.toInt().toChar() }.joinToString("")
        return !prefix.startsWith(SQLITE_HEADER_PREFIX)
    }
}

internal actual fun createPlatformDatabaseInspector(config: DatabasePluginConfig): DatabaseInspector =
    IosDatabaseInspector(config)
