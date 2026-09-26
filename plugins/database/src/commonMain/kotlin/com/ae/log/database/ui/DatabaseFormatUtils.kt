package com.ae.log.database.ui

import com.ae.log.utils.TimeUtils

private const val COMMA_SEPARATOR = ", "

public object DatabaseFormatUtils {
    /**
     * Formats bytes into human-readable size (e.g. "12.4 MB", "850 KB").
     */
    public fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "${kotlin.math.round(gb * 10) / 10.0} GB"
            mb >= 1.0 -> "${kotlin.math.round(mb * 10) / 10.0} MB"
            kb >= 1.0 -> "${kotlin.math.round(kb * 10) / 10.0} KB"
            else -> "$bytes B"
        }
    }

    /**
     * Formats an epoch timestamp into time-of-day (HH:mm:ss AM/PM).
     */
    public fun formatTime(timestampMs: Long): String {
        if (timestampMs <= 0) return ""
        return TimeUtils.formatTimestamp(timestampMs)
    }

    /**
     * Formats a row map into pretty-printed JSON.
     */
    public fun rowToJson(row: Map<String, String?>): String =
        buildString {
            appendLine("{")
            val entries = row.entries.toList()
            entries.forEachIndexed { index, (key, value) ->
                append("  \"$key\": ")
                if (value == null) {
                    append("null")
                } else if (value.toLongOrNull() != null || value.toDoubleOrNull() != null) {
                    append(value)
                } else if (value.equals("true", ignoreCase = true) || value.equals("false", ignoreCase = true)) {
                    append(value.lowercase())
                } else {
                    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                    append("\"$escaped\"")
                }
                if (index < entries.lastIndex) append(",")
                appendLine()
            }
            append("}")
        }

    /**
     * Formats a row map into a standard SQL INSERT statement.
     */
    public fun rowToSqlInsert(
        tableName: String,
        row: Map<String, String?>,
    ): String {
        val cols = row.keys.joinToString(COMMA_SEPARATOR) { "\"$it\"" }
        val vals =
            row.values.joinToString(COMMA_SEPARATOR) { v ->
                if (v == null) {
                    "NULL"
                } else if (v.toLongOrNull() != null || v.toDoubleOrNull() != null) {
                    v
                } else {
                    "'${v.replace("'", "''")}'"
                }
            }
        return "INSERT INTO \"$tableName\" ($cols) VALUES ($vals);"
    }

    /**
     * Formats a single database log entry into a plain-text representation for clipboard copying.
     */
    public fun formatDatabaseLogForCopy(log: com.ae.log.database.model.DatabaseLogEntry): String =
        buildString {
            append("[${formatTime(log.timestamp)}] ")
            append("${log.operation} ")
            if (!log.tableName.isNullOrBlank()) append("table: ${log.tableName} ")
            append("(${log.durationMs}ms)\n")
            append(log.sql)
            if (!log.isSuccess && !log.errorMessage.isNullOrBlank()) {
                append("\nError: ${log.errorMessage}")
            }
        }

    /**
     * Formats multiple database log entries into a plain-text representation for clipboard copying.
     */
    public fun formatDatabaseLogsForCopy(logs: List<com.ae.log.database.model.DatabaseLogEntry>): String =
        logs.joinToString("\n\n") { formatDatabaseLogForCopy(it) }
}
