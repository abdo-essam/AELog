package com.ae.devlens.plugins.logs.ui

import androidx.compose.ui.graphics.Color
import com.ae.devlens.plugins.logs.model.LogEntry
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

/**
 * Utility functions for log formatting and coloring.
 */
internal object LogUtils {
    fun formatTimestamp(timestamp: Long): String = runCatching {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        buildString {
            append(localDateTime.hour.toString().padStart(2, '0'))
            append(":")
            append(localDateTime.minute.toString().padStart(2, '0'))
            append(":")
            append(localDateTime.second.toString().padStart(2, '0'))
        }
    }.getOrDefault("")

    fun formatLogForCopy(log: LogEntry): String = buildString {
        appendLine("[${formatTimestamp(log.timestamp)}] [${log.displayTag}]")
        appendLine("Tag: ${log.tag}")
        log.httpMethod?.let { appendLine("Method: $it") }
        log.url?.let { appendLine("URL: $it") }
        log.httpStatusCode?.let { appendLine("Status: $it") }
        log.jsonBody?.let {
            appendLine("Body:")
            appendLine(it)
        } ?: run {
            appendLine("Message:")
            appendLine(log.cleanMessage)
        }
    }

    fun formatAllLogsForCopy(logs: List<LogEntry>): String = logs.joinToString("\n\n") { log ->
        buildString {
            appendLine("=".repeat(50))
            append(formatLogForCopy(log))
        }
    }

    fun getMethodColor(method: String): Color = when (method) {
        "GET" -> Color(0xFF4CAF50)
        "POST" -> Color(0xFF2196F3)
        "PUT" -> Color(0xFFFF9800)
        "DELETE" -> Color(0xFFE53935)
        "PATCH" -> Color(0xFF9C27B0)
        else -> Color(0xFF9E9E9E)
    }

    fun getStatusCodeColor(statusCode: Int): Color = when (statusCode) {
        in 200..299 -> Color(0xFF4CAF50)
        in 300..399 -> Color(0xFFFF9800)
        in 400..499 -> Color(0xFFE53935)
        else -> Color(0xFF9C27B0)
    }

    fun getLogTypeColor(log: LogEntry): Pair<Color, Color> {
        val mainColor = when {
            log.isAnalytics -> Color(0xFFFF6D00)
            log.isError -> Color(0xFFE53935)
            log.isResponse -> Color(0xFF4CAF50)
            log.isRequest -> Color(0xFF2196F3)
            log.isNetworkLog -> Color(0xFF9C27B0)
            else -> Color(0xFF9E9E9E)
        }
        val bgColor = when {
            log.isAnalytics -> Color(0xFFFFF3E0)
            log.isError -> Color(0xFFFFEBEE)
            log.isResponse -> Color(0xFFE8F5E9)
            log.isRequest -> Color(0xFFE3F2FD)
            log.isNetworkLog -> Color(0xFFF3E5F5)
            else -> Color(0xFFFAFAFA)
        }
        return mainColor to bgColor
    }

    fun getBadgeLabel(log: LogEntry): String = when {
        log.isAnalytics -> "Analytics"
        log.isError -> "ERR"
        log.isResponse -> "RES"
        log.isRequest -> "REQ"
        log.isNetworkLog -> "NET"
        else -> "LOG"
    }
}
