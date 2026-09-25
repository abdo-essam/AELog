package com.ae.log.database.inspector

import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import java.io.File
import java.io.FileInputStream

private const val PRAGMA_KEYWORD = "PRAGMA"
private const val PROP_USER_DIR = "user.dir"

internal class JvmDatabaseInspector(
    private val config: DatabasePluginConfig,
) : DatabaseInspector {
    private val registeredDatabases = mutableListOf<DbInfo>()

    override fun registerDatabase(dbInfo: DbInfo) {
        if (registeredDatabases.none { it.path == dbInfo.path }) {
            registeredDatabases.add(dbInfo)
        }
    }

    override fun listDatabases(): List<DbInfo> {
        val result = mutableListOf<DbInfo>()

        // 1. Scan user.home/.ae_databases and working directory
        val searchDirs =
            listOf(
                File(System.getProperty("user.home"), ".ae_databases"),
                File(System.getProperty(PROP_USER_DIR), "databases"),
                File(System.getProperty(PROP_USER_DIR)),
            )

        searchDirs.forEach { dir ->
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (isSqliteFile(file) &&
                        !isAuxiliaryFile(file.name) &&
                        result.none { it.path == file.absolutePath }
                    ) {
                        result.add(
                            DbInfo(
                                name = file.name,
                                path = file.absolutePath,
                                isEncrypted = isEncryptedSqliteFile(file),
                                sizeBytes = file.length(),
                            ),
                        )
                    }
                }
            }
        }

        // 2. Scan additional search paths
        config.additionalSearchPaths.forEach { path ->
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) {
                dir.listFiles()?.forEach { file ->
                    if (isSqliteFile(file) &&
                        !isAuxiliaryFile(file.name) &&
                        result.none { it.path == file.absolutePath }
                    ) {
                        result.add(
                            DbInfo(
                                name = file.name,
                                path = file.absolutePath,
                                isEncrypted = isEncryptedSqliteFile(file),
                                sizeBytes = file.length(),
                            ),
                        )
                    }
                }
            }
        }

        // 3. Registered databases
        registeredDatabases.forEach { reg ->
            if (result.none { it.path == reg.path }) {
                result.add(reg)
            }
        }

        return result
    }

    override fun listTables(dbInfo: DbInfo): List<DbTable> = emptyList()

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

        val file = File(dbInfo.path)
        if (!file.exists()) {
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

        val err =
            QueryResult.error(
                "JVM runtime database inspector requires JDBC or a custom DatabaseInspector implementation.",
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

    private fun isSqliteFile(file: File): Boolean {
        if (!file.isFile) return false
        val ext = file.extension.lowercase()
        return ext == "db" || ext == "sqlite" || ext == "sqlite3"
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

    private fun isEncryptedSqliteFile(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(16)
                val read = fis.read(header)
                if (read < 16) false else !header.toString(Charsets.UTF_8).startsWith("SQLite format 3")
            }
        } catch (_: Exception) {
            false
        }
    }
}

internal actual fun createPlatformDatabaseInspector(config: DatabasePluginConfig): DatabaseInspector =
    JvmDatabaseInspector(config)
