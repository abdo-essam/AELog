package com.ae.log.plugins.log.model

import androidx.compose.runtime.Immutable
import com.ae.log.core.utils.IdGenerator
import kotlin.time.Clock

/**
 * Represents a single log entry captured by AELog.
 *
 * Intentionally simple — severity, tag, and message.
 * Network traffic and analytics events are captured by their dedicated plugins,
 * not heuristically detected from log messages.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Immutable
public data class LogEntry(
    val id: String = IdGenerator.next(),
    val severity: LogSeverity,
    val tag: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
