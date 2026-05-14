package com.ae.log.logs.ui

import com.ae.log.logs.model.LogEntry
import com.ae.log.utils.TimeUtils

internal object LogUtils {
    fun formatTimestamp(timestamp: Long): String = TimeUtils.formatTimestamp(timestamp)

    fun formatLogForCopy(log: LogEntry): String =
        buildString {
            appendLine("[${formatTimestamp(log.timestamp)}] [${log.severity.label}] ${log.tag}")
            appendLine(log.message)
        }

    fun formatAllLogsForCopy(logs: List<LogEntry>): String =
        logs.joinToString("\n\n") { "${"=".repeat(50)}\n${formatLogForCopy(it)}" }
}
