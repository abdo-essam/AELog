package com.ae.log.database

import com.ae.log.database.inspector.detectOperation
import com.ae.log.database.model.DatabaseLogEntry
import com.ae.log.database.model.DatabaseOperation
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Thread-safe in-memory ring-buffer for capturing database query operations and errors.
 */
public object DatabaseLogRecorder {
    private const val MAX_LOGS = 500

    private val lock = SynchronizedObject()
    private val buffer = ArrayDeque<DatabaseLogEntry>(MAX_LOGS)
    private val _logs = MutableStateFlow<List<DatabaseLogEntry>>(emptyList())
    public val logs: StateFlow<List<DatabaseLogEntry>> = _logs.asStateFlow()

    /**
     * Records a database operation entry into the log history.
     */
    public fun record(
        databaseName: String,
        sql: String,
        durationMs: Long,
        tableName: String? = null,
        isSuccess: Boolean = true,
        errorMessage: String? = null,
        affectedRows: Long? = null,
        engine: String = "SQLite",
    ) {
        if (isInternalSystemQuery(sql)) {
            return
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val entry =
            DatabaseLogEntry(
                id = generateLogId(now),
                databaseName = databaseName,
                tableName = tableName ?: extractTableName(sql),
                sql = sql,
                operation = if (!isSuccess) DatabaseOperation.ERROR else detectOperation(sql),
                durationMs = durationMs,
                timestamp = now,
                isSuccess = isSuccess,
                errorMessage = errorMessage,
                affectedRows = affectedRows,
                engine = engine,
            )

        synchronized(lock) {
            if (buffer.size >= MAX_LOGS) {
                buffer.removeLast()
            }
            buffer.addFirst(entry)
            _logs.value = buffer.toList()
        }
    }

    /**
     * Clears all recorded database logs.
     */
    public fun clear() {
        synchronized(lock) {
            buffer.clear()
            _logs.value = emptyList()
        }
    }

    private fun generateLogId(timestamp: Long): String = "db_${timestamp}_${Random.nextInt(1000, 9999)}"

    private fun extractTableName(sql: String): String? {
        val len = sql.length
        var i = 0
        while (i < len) {
            while (i < len && sql[i].isWhitespace()) i++
            if (i >= len) break

            val start = i
            while (i < len && !sql[i].isWhitespace()) i++
            val wordLen = i - start

            if (wordLen == 4) {
                val isFrom = sql.regionMatches(start, "FROM", 0, 4, ignoreCase = true)
                val isInto = sql.regionMatches(start, "INTO", 0, 4, ignoreCase = true)
                if (isFrom || isInto) {
                    return extractNextToken(sql, i)
                }
            } else if (wordLen == 6) {
                if (sql.regionMatches(start, "UPDATE", 0, 6, ignoreCase = true)) {
                    return extractNextToken(sql, i)
                }
            } else if (wordLen == 5) {
                if (sql.regionMatches(start, "TABLE", 0, 5, ignoreCase = true)) {
                    return extractNextToken(sql, i)
                }
            }
        }
        return null
    }

    private fun extractNextToken(
        sql: String,
        startIdx: Int,
    ): String? {
        var i = startIdx
        val len = sql.length
        while (i < len && sql[i].isWhitespace()) i++
        if (i >= len) return null
        val tokenStart = i
        while (i < len && !sql[i].isWhitespace()) i++
        val rawToken = sql.substring(tokenStart, i)
        return rawToken.trim('\"', '`', '\'', ';', '(', ')')
    }

    private fun isInternalSystemQuery(sql: String): Boolean {
        var start = 0
        val len = sql.length
        while (start < len && sql[start].isWhitespace()) start++
        if (start >= len) return false

        if (sql.regionMatches(start, "PRAGMA", 0, 6, ignoreCase = true) ||
            sql.regionMatches(start, "BEGIN", 0, 5, ignoreCase = true) ||
            sql.regionMatches(start, "COMMIT", 0, 6, ignoreCase = true) ||
            sql.regionMatches(start, "END TRANSACTION", 0, 15, ignoreCase = true) ||
            sql.regionMatches(start, "END;", 0, 4, ignoreCase = true)
        ) {
            return true
        }

        return sql.contains("room_table_modification_log", ignoreCase = true) ||
            sql.contains("room_master_table", ignoreCase = true) ||
            sql.contains("sqlite_master", ignoreCase = true) ||
            sql.contains("sqlite_schema", ignoreCase = true) ||
            sql.contains("sqlite_sequence", ignoreCase = true)
    }
}
