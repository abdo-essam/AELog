package com.ae.log.core.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

public object TimeUtils {
    public fun formatTimestamp(timestamp: Long): String =
        runCatching {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${dt.hour.toString().padStart(
                2,
                '0',
            )}:${dt.minute.toString().padStart(2,'0')}:${dt.second.toString().padStart(2,'0')}"
        }.getOrDefault("")
}
