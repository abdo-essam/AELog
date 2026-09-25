package com.ae.log.database

import com.ae.log.database.inspector.detectOperation
import com.ae.log.database.model.DatabaseLogEntry
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
                operation = if (!isSuccess) "ERROR" else detectOperation(sql),
                durationMs = durationMs,
                timestamp = now,
                isSuccess = isSuccess,
                errorMessage = errorMessage,
                affectedRows = affectedRows,
                engine = engine,
            )

        val current = _logs.value.toMutableList()
        current.add(0, entry)
        if (current.size > MAX_LOGS) {
            _logs.value = current.take(MAX_LOGS)
        } else {
            _logs.value = current
        }
    }

    /**
     * Clears all recorded database logs.
     */
    public fun clear() {
        _logs.value = emptyList()
    }

    private fun generateLogId(timestamp: Long): String = "db_${timestamp}_${Random.nextInt(1000, 9999)}"

    private fun extractTableName(sql: String): String? {
        val words = sql.trim().split(Regex("\\s+"))
        val fromIdx = words.indexOfFirst { it.equals("FROM", ignoreCase = true) }
        if (fromIdx >= 0 && fromIdx + 1 < words.size) {
            return words[fromIdx + 1].trim('\"', '`', '\'', ';', '(')
        }
        val intoIdx = words.indexOfFirst { it.equals("INTO", ignoreCase = true) }
        if (intoIdx >= 0 && intoIdx + 1 < words.size) {
            return words[intoIdx + 1].trim('\"', '`', '\'', ';', '(')
        }
        val updateIdx = words.indexOfFirst { it.equals("UPDATE", ignoreCase = true) }
        if (updateIdx >= 0 && updateIdx + 1 < words.size) {
            return words[updateIdx + 1].trim('\"', '`', '\'', ';', '(')
        }
        val tableIdx = words.indexOfFirst { it.equals("TABLE", ignoreCase = true) }
        if (tableIdx >= 0 && tableIdx + 1 < words.size) {
            return words[tableIdx + 1].trim('\"', '`', '\'', ';', '(')
        }
        return null
    }

    private fun isInternalSystemQuery(sql: String): Boolean {
        val trimmed = sql.trim().uppercase()
        if (trimmed.startsWith("PRAGMA") ||
            trimmed.startsWith("BEGIN") ||
            trimmed.startsWith("COMMIT") ||
            trimmed.startsWith("END TRANSACTION") ||
            trimmed.startsWith("END;")
        ) {
            return true
        }
        val lower = sql.lowercase()
        return lower.contains("room_table_modification_log") ||
            lower.contains("room_master_table") ||
            lower.contains("sqlite_master") ||
            lower.contains("sqlite_schema") ||
            lower.contains("sqlite_sequence")
    }
}
