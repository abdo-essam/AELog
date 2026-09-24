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

    override fun listTables(dbInfo: DbInfo): List<DbTable> {
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
            return QueryResult.error(e.message ?: "Write operation disallowed")
        }

        // Check if file exists in the iOS sandbox
        if (!fileManager.fileExistsAtPath(dbInfo.path)) {
            return QueryResult.error("Database file not found: ${dbInfo.path}")
        }

        if (dbInfo.isEncrypted && config.passphraseProvider?.getPassphrase(dbInfo.name) == null) {
            return QueryResult.error("Database is encrypted. Please configure a PassphraseProvider.")
        }

        return QueryResult.error(
            "SQLite runtime query engine is available on Android / JVM. On iOS, connect a custom DatabaseInspector or registered snapshot.",
        )
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
