package com.ae.log.database.inspector

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.ae.log.database.DatabaseAppContextHolder
import com.ae.log.database.DatabaseLogRecorder
import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import java.io.File
import java.io.FileInputStream
import kotlin.system.measureTimeMillis

private const val PRAGMA_KEYWORD = "PRAGMA"
private const val SQLITE_SYSTEM_PREFIX = "sqlite_"
private const val ANDROID_METADATA_TABLE = "android_metadata"
private const val SQLITE_HEADER_PREFIX = "SQLite format 3"

internal class AndroidDatabaseInspector(
    private val context: Context?,
    private val config: DatabasePluginConfig,
) : DatabaseInspector {
    private val currentContext: Context?
        get() = context ?: DatabaseAppContextHolder.context

    private val registeredDatabases = mutableListOf<DbInfo>()

    override fun registerDatabase(dbInfo: DbInfo) {
        if (registeredDatabases.none { it.path == dbInfo.path }) {
            registeredDatabases.add(dbInfo)
        }
    }

    override fun listDatabases(): List<DbInfo> {
        val result = mutableListOf<DbInfo>()
        val ctx = currentContext

        if (ctx != null) {
            // 1. Scan context.databaseList()
            ctx.databaseList()?.forEach { dbName ->
                if (!isAuxiliaryFile(dbName)) {
                    val dbFile = ctx.getDatabasePath(dbName)
                    if (dbFile.exists() && dbFile.isFile && result.none { it.path == dbFile.absolutePath }) {
                        result.add(buildDbInfo(dbFile))
                    }
                }
            }

            // 2. Scan databases directory directly
            val databasesDir = ctx.getDatabasePath("probe").parentFile
            if (databasesDir != null && databasesDir.exists() && databasesDir.isDirectory) {
                databasesDir.listFiles()?.forEach { file ->
                    if (file.isFile && !isAuxiliaryFile(file.name) && result.none { it.path == file.absolutePath }) {
                        if (isSqliteFileOrHeader(file)) {
                            result.add(buildDbInfo(file))
                        }
                    }
                }
            }

            // 3. Scan filesDir and noBackupFilesDir (common for custom DB locations)
            listOfNotNull(ctx.filesDir, ctx.noBackupFilesDir).forEach { dir ->
                if (dir.exists() && dir.isDirectory) {
                    dir.walkTopDown().maxDepth(2).forEach { file ->
                        if (file.isFile &&
                            !isAuxiliaryFile(file.name) &&
                            result.none { it.path == file.absolutePath }
                        ) {
                            if (isSqliteFileOrHeader(file)) {
                                result.add(buildDbInfo(file))
                            }
                        }
                    }
                }
            }
        }

        // 4. Scan additional search paths configured by user
        config.additionalSearchPaths.forEach { searchPath ->
            val dir = File(searchPath)
            if (dir.exists() && dir.isDirectory) {
                dir.walkTopDown().maxDepth(2).forEach { file ->
                    if (file.isFile && !isAuxiliaryFile(file.name) && result.none { it.path == file.absolutePath }) {
                        if (isSqliteFileOrHeader(file)) {
                            result.add(buildDbInfo(file))
                        }
                    }
                }
            }
        }

        // 5. Include registered databases (only if they actually exist on disk)
        registeredDatabases.forEach { reg ->
            val file = File(reg.path)
            if (file.exists() && file.isFile && result.none { it.path == reg.path }) {
                result.add(buildDbInfo(file).copy(name = reg.name))
            }
        }

        return result
    }

    private fun buildDbInfo(file: File): DbInfo {
        val isEncrypted = isEncryptedSqliteFile(file)
        val engine = if (isEncrypted) "SQLCipher" else "SQLite"
        var framework: String? = null
        var tableCount = -1
        var version = ""

        if (!isEncrypted && file.exists() && file.canRead()) {
            try {
                val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
                try {
                    db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { c ->
                        var count = 0
                        while (c.moveToNext()) {
                            val name = c.getString(0) ?: ""
                            if (name == "room_master_table") framework = "Room"
                            if (name.startsWith("sqldelight_")) framework = "SQLDelight"
                            if (!name.startsWith(SQLITE_SYSTEM_PREFIX) && name != ANDROID_METADATA_TABLE) {
                                count++
                            }
                        }
                        tableCount = count
                    }
                    db.rawQuery("SELECT sqlite_version()", null).use { c ->
                        if (c.moveToFirst()) version = c.getString(0) ?: ""
                    }
                } finally {
                    db.close()
                }
            } catch (_: Exception) {
            }
        }

        return DbInfo(
            name = file.name,
            path = file.absolutePath,
            isEncrypted = isEncrypted,
            sizeBytes = file.length(),
            engine = engine,
            framework = framework,
            tableCount = tableCount,
            version = version,
        )
    }

    override fun listTables(dbInfo: DbInfo): List<DbTable> {
        val db = openDatabase(dbInfo) ?: return emptyList()
        return try {
            val tables = mutableListOf<DbTable>()
            val query = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name"
            db.rawQuery(query, null).use { cursor ->
                while (cursor.moveToNext()) {
                    val tableName = cursor.getString(0) ?: continue
                    val isSystem = tableName.startsWith(SQLITE_SYSTEM_PREFIX) || tableName == ANDROID_METADATA_TABLE

                    // Retrieve columns for table via PRAGMA table_info
                    val columns = mutableListOf<String>()
                    try {
                        db.rawQuery("PRAGMA table_info(\"$tableName\")", null).use { pragmaCursor ->
                            val nameIndex = pragmaCursor.getColumnIndex("name")
                            while (pragmaCursor.moveToNext()) {
                                if (nameIndex >= 0) {
                                    columns.add(pragmaCursor.getString(nameIndex))
                                }
                            }
                        }
                    } catch (_: Exception) {
                    }

                    // Retrieve row count
                    var rowCount = -1L
                    try {
                        db.rawQuery("SELECT COUNT(*) FROM \"$tableName\"", null).use { countCursor ->
                            if (countCursor.moveToFirst()) {
                                rowCount = countCursor.getLong(0)
                            }
                        }
                    } catch (_: Exception) {
                    }

                    tables.add(
                        DbTable(
                            name = tableName,
                            rowCount = rowCount,
                            columns = columns,
                            isSystemTable = isSystem,
                        ),
                    )
                }
            }
            tables
        } catch (_: Exception) {
            emptyList()
        } finally {
            closeQuietly(db)
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
                DatabaseLogRecorder.record(
                    databaseName = dbInfo.name,
                    sql = sql,
                    durationMs = 0L,
                    isSuccess = false,
                    errorMessage = err.errorMessage,
                )
            }
            return err
        }

        val db = openDatabase(dbInfo)
        if (db == null) {
            val err = QueryResult.error("Failed to open database: ${dbInfo.name}")
            if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
                DatabaseLogRecorder.record(
                    databaseName = dbInfo.name,
                    sql = sql,
                    durationMs = 0L,
                    isSuccess = false,
                    errorMessage = err.errorMessage,
                )
            }
            return err
        }
        val isWrite = isWriteStatement(sql)

        val queryResult =
            try {
                var executionTime = 0L
                if (isWrite) {
                    var affected = 0L
                    executionTime =
                        measureTimeMillis {
                            val statement = db.compileStatement(sql)
                            try {
                                args.forEachIndexed { index, arg ->
                                    statement.bindString(index + 1, arg)
                                }
                                val result = statement.executeUpdateDelete()
                                affected = result.toLong()
                            } finally {
                                statement.close()
                            }
                        }
                    QueryResult.writeSuccess(affectedRows = affected, durationMs = executionTime)
                } else {
                    var res: QueryResult
                    executionTime =
                        measureTimeMillis {
                            val rawArgs = if (args.isEmpty()) null else args.toTypedArray()
                            db.rawQuery(sql, rawArgs).use { cursor ->
                                val colNames = cursor.columnNames.toList()
                                val rows = mutableListOf<List<String?>>()
                                while (cursor.moveToNext()) {
                                    val row =
                                        (0 until cursor.columnCount).map { i ->
                                            if (cursor.isNull(i)) null else cursor.getString(i)
                                        }
                                    rows.add(row)
                                }
                                res = QueryResult.success(columns = colNames, rows = rows, durationMs = 0L)
                            }
                        }
                    res.copy(executionDurationMs = executionTime)
                }
            } catch (e: Exception) {
                QueryResult.error(e.message ?: "SQL execution error")
            } finally {
                closeQuietly(db)
            }

        if (!sql.trimStart().uppercase().startsWith(PRAGMA_KEYWORD)) {
            DatabaseLogRecorder.record(
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

    private fun openDatabase(dbInfo: DbInfo): SQLiteDatabase? {
        val file = File(dbInfo.path)
        if (!file.exists()) return null

        val flags = SQLiteDatabase.OPEN_READWRITE

        // Handle encrypted database if passphrase provider is present
        if (dbInfo.isEncrypted) {
            val passphrase = config.passphraseProvider?.getPassphrase(dbInfo.name)
            if (passphrase != null) {
                val openedSqlCipher = openWithSqlCipherReflection(dbInfo.path, String(passphrase))
                if (openedSqlCipher != null) return openedSqlCipher
            }
        }

        return try {
            val db = SQLiteDatabase.openDatabase(dbInfo.path, null, flags)
            // Configure WAL busy timeout to avoid immediate locks
            try {
                db.execSQL("PRAGMA busy_timeout = ${config.busyTimeoutMs};")
            } catch (_: Exception) {
            }
            db
        } catch (_: Exception) {
            try {
                // Fallback to read-only mode if read-write failed
                SQLiteDatabase.openDatabase(dbInfo.path, null, SQLiteDatabase.OPEN_READONLY)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun openWithSqlCipherReflection(
        path: String,
        passphrase: String,
    ): SQLiteDatabase? =
        try {
            val sqlCipherClass = Class.forName("net.sqlcipher.database.SQLiteDatabase")
            val openMethod =
                sqlCipherClass.getMethod(
                    "openDatabase",
                    String::class.java,
                    String::class.java,
                    Class.forName("net.sqlcipher.database.SQLiteDatabase\$CursorFactory"),
                    Int::class.javaPrimitiveType,
                )
            // SQLCipher SQLiteDatabase is not standard android.database.sqlite.SQLiteDatabase,
            // so we return null if standard interface cannot wrap it directly.
            null
        } catch (_: Exception) {
            null
        }

    private fun closeQuietly(db: SQLiteDatabase) {
        try {
            db.close()
        } catch (_: Exception) {
        }
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

    private fun isSqliteFileOrHeader(file: File): Boolean {
        val ext = file.extension.lowercase()
        if (ext == "db" || ext == "sqlite" || ext == "sqlite3") return true
        if (!file.exists() || file.length() < 16) return false
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(16)
                val read = fis.read(header)
                read >= 16 && header.toString(Charsets.UTF_8).startsWith(SQLITE_HEADER_PREFIX)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun isEncryptedSqliteFile(file: File): Boolean {
        if (!file.exists() || file.length() < 16) return false
        return try {
            FileInputStream(file).use { fis ->
                val header = ByteArray(16)
                val read = fis.read(header)
                if (read < 16) false else !header.toString(Charsets.UTF_8).startsWith(SQLITE_HEADER_PREFIX)
            }
        } catch (_: Exception) {
            false
        }
    }
}

internal actual fun createPlatformDatabaseInspector(config: DatabasePluginConfig): DatabaseInspector =
    AndroidDatabaseInspector(
        context = DatabaseAppContextHolder.context,
        config = config,
    )
