package com.ae.log.crashes.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Represents a single captured crash event.
 *
 * All fields are immutable. Timestamps are epoch-millis.
 * [stackTrace] is the full formatted stack trace string.
 * [deviceInfo] is a snapshot of device / OS metadata captured at crash time.
 */
@Immutable
@Serializable
public data class CrashEvent(
    val id: String,
    val timestamp: Long,
    val exceptionType: String,
    val message: String,
    val stackTrace: String,
    val threadName: String,
    val isFatal: Boolean,
    /** Device and OS metadata captured at the moment of the crash. */
    val deviceInfo: DeviceInfo = DeviceInfo(
        model = "unknown",
        osVersion = "unknown",
        appVersion = "unknown",
        buildNumber = "unknown",
    ),
)

