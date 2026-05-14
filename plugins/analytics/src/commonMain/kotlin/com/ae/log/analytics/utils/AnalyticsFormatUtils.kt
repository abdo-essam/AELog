package com.ae.log.analytics.utils

import com.ae.log.analytics.model.AnalyticsEvent
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

internal fun Long.toTimeLabel(): String {
    val t =
        kotlin.time.Instant
            .fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())

    fun Int.pad() = toString().padStart(2, '0')
    return "${t.hour.pad()}:${t.minute.pad()}:${t.second.pad()}"
}

internal fun Long.toFullTimeLabel(): String {
    val t =
        kotlin.time.Instant
            .fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.currentSystemDefault())

    fun Int.pad() = toString().padStart(2, '0')
    return "${t.date} ${t.hour.pad()}:${t.minute.pad()}:${t.second.pad()}"
}

internal fun AnalyticsEvent.toClipboardText(): String =
    buildString {
        appendLine("Event: $name")
        source?.let { appendLine("Source: ${it.sourceName}") }
        appendLine("Time: ${timestamp.toFullTimeLabel()}")
        if (properties.isNotEmpty()) {
            appendLine("Properties:")
            properties.entries.forEach { (k, v) -> appendLine("  $k = $v") }
        }
    }
