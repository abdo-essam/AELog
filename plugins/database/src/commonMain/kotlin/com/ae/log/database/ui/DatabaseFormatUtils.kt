package com.ae.log.database.ui

import com.ae.log.utils.TimeUtils

internal object DatabaseFormatUtils {
    /**
     * Formats bytes into human-readable size (e.g. "12.4 MB", "850 KB").
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> "${(gb * 10).toLong() / 10.0} GB"
            mb >= 1.0 -> "${(mb * 10).toLong() / 10.0} MB"
            kb >= 1.0 -> "${(kb * 10).toLong() / 10.0} KB"
            else -> "$bytes B"
        }
    }

    /**
     * Formats an epoch timestamp into time-of-day (HH:mm:ss).
     */
    fun formatTime(timestampMs: Long): String {
        if (timestampMs <= 0) return ""
        val formatted = TimeUtils.formatTimestamp(timestampMs)
        // Extract time portion if full datetime is returned
        return if (formatted.contains(" ")) {
            formatted.substringAfter(" ")
        } else {
            formatted
        }
    }

    /**
     * Formats a row map into pretty-printed JSON.
     */
    fun rowToJson(row: Map<String, String?>): String =
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
    fun rowToSqlInsert(tableName: String, row: Map<String, String?>): String {
        val cols = row.keys.joinToString(", ") { "\"$it\"" }
        val vals = row.values.joinToString(", ") { v ->
            if (v == null) "NULL"
            else if (v.toLongOrNull() != null || v.toDoubleOrNull() != null) v
            else "'${v.replace("'", "''")}'"
        }
        return "INSERT INTO \"$tableName\" ($cols) VALUES ($vals);"
    }
}
