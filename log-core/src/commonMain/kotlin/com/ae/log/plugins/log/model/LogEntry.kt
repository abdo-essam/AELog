package com.ae.log.plugins.log.model

import androidx.compose.runtime.Immutable
import kotlin.time.Clock

/**
 * Represents a single log entry captured by AELog.
 *
 * This is a simple data holder. Parsing and classification logic
 * are provided via extension properties in LogClassifier.
 */
@OptIn(kotlin.time.ExperimentalTime::class)
@Immutable
public data class LogEntry(
    val id: String =
        com.ae.log.core.utils.IdGenerator
            .generateId(),
    val severity: LogSeverity,
    val tag: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
