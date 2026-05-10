package com.ae.log.core.utils

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

public object TimeUtils {
    public fun formatTimestamp(timestamp: Long): String =
        runCatching {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val dt = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val isPM = dt.hour >= 12
            val hour12 =
                when {
                    dt.hour == 0 -> 12
                    dt.hour > 12 -> dt.hour - 12
                    else -> dt.hour
                }
            val amPm = if (isPM) "PM" else "AM"
            val h = hour12.toString().padStart(2, '0')
            val m = dt.minute.toString().padStart(2, '0')
            val s = dt.second.toString().padStart(2, '0')
            "$h:$m:$s $amPm"
        }.getOrDefault("")
}
