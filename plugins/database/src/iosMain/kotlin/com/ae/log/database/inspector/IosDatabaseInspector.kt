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

private const val SQLITE_HEADER_PREFIX = "SQLite format 3"

@OptIn(BetaInteropApi::class)
internal class IosDatabaseInspector(
    private val config: DatabasePluginConfig,
) : DatabaseInspector {
    private val fileManager = NSFileManager.defaultManager
    private val registeredDatabases = mutableListOf<DbInfo>()
    private val virtualTables = mutableMapOf<String, List<DbTable>>()

    override fun registerDatabase(dbInfo: DbInfo) {
        if (registeredDatabases.none { it.path == dbInfo.path }) {
            registeredDatabases.add(dbInfo)
        }
    }

    fun registerVirtualTable(
        dbName: String,
        table: DbTable,
    ) {
        val current = virtualTables[dbName]?.toMutableList() ?: mutableListOf()
        current.removeAll { it.name == table.name }
        current.add(table)
        virtualTables[dbName] = current
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

    override fun listTables(dbInfo: DbInfo): List<DbTable> {
        val virtual = virtualTables[dbInfo.name]
        if (!virtual.isNullOrEmpty()) {
            return virtual
        }

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
            recordQueryLog(dbInfo.name, sql, 0L, false, err.errorMessage)
            return err
        }

        if (!fileManager.fileExistsAtPath(dbInfo.path)) {
            val err = QueryResult.error("Database file not found: ${dbInfo.path}")
            recordQueryLog(dbInfo.name, sql, 0L, false, err.errorMessage)
            return err
        }

        if (dbInfo.isEncrypted && config.passphraseProvider?.getPassphrase(dbInfo.name) == null) {
            val err = QueryResult.error("Database is encrypted. Please configure a PassphraseProvider.")
            recordQueryLog(dbInfo.name, sql, 0L, false, err.errorMessage)
            return err
        }

        val err =
            QueryResult.error(
                "SQLite runtime query engine is available on Android / JVM. " +
                    "On iOS, connect a custom DatabaseInspector or registered snapshot.",
            )
        recordQueryLog(dbInfo.name, sql, 0L, false, err.errorMessage)
        return err
    }

    private fun recordQueryLog(
        dbName: String,
        sql: String,
        durationMs: Long,
        isSuccess: Boolean,
        errorMessage: String?,
        affectedRows: Long? = null,
    ) {
        if (!sql.trimStart().uppercase().startsWith("PRAGMA")) {
            com.ae.log.database.DatabaseLogRecorder.record(
                databaseName = dbName,
                sql = sql,
                durationMs = durationMs,
                isSuccess = isSuccess,
                errorMessage = errorMessage,
                affectedRows = affectedRows,
            )
        }
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
