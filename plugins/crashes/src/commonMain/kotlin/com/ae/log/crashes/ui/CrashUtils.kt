package com.ae.log.crashes.ui

import com.ae.log.crashes.model.CrashEvent
import com.ae.log.utils.TimeUtils

internal object CrashUtils {
    fun formatTimestamp(epochMillis: Long): String = TimeUtils.formatTimestamp(epochMillis)

    fun formatEventForCopy(event: CrashEvent): String =
        buildString {
            appendLine("[${if (event.isFatal) "FATAL" else "NON-FATAL"}] ${event.exceptionType}")
            appendLine("Thread : ${event.threadName}")
            appendLine("Time   : ${formatTimestamp(event.timestamp)}")
            if (event.message.isNotBlank()) appendLine("Message: ${event.message}")
            appendLine()
            append(event.stackTrace)
        }

    fun formatAllEventsForCopy(events: List<CrashEvent>): String =
        events.joinToString("\n\n${"-".repeat(60)}\n\n") { formatEventForCopy(it) }
}
