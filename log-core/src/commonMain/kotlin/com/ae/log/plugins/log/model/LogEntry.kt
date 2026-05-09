package com.ae.log.plugins.log.model

import androidx.compose.runtime.Immutable

/**
 * Represents a single log entry captured by AELog.
 *
 * Intentionally simple — severity, tag, and message.
 * Network traffic and analytics events are captured by their dedicated plugins,
 * not heuristically detected from log messages.
 *
 * `id` and `timestamp` must always be supplied explicitly; there are no defaults
 * to prevent accidental regeneration on [copy].
 */
@Immutable
public data class LogEntry(
    val id: String,
    val severity: LogSeverity,
    val tag: String,
    val message: String,
    val timestamp: Long,
)

