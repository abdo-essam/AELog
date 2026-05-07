package com.ae.log.plugins.log.ui

import com.ae.log.core.utils.TimeUtils
import com.ae.log.plugins.log.model.LogEntry

internal object LogUtils {
    fun formatTimestamp(timestamp: Long): String = TimeUtils.formatTimestamp(timestamp)

    fun formatLogForCopy(log: LogEntry): String = buildString {
        appendLine("[${formatTimestamp(log.timestamp)}] [${log.severity.label}] ${log.tag}")
        appendLine(log.message)
    }

    fun formatAllLogsForCopy(logs: List<LogEntry>): String =
        logs.joinToString("\n\n") { "${"=".repeat(50)}\n${formatLogForCopy(it)}" }
}
