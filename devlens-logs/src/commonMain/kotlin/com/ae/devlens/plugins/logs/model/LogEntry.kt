package com.ae.devlens.plugins.logs.model

import kotlin.time.Clock

/**
 * Represents a single log entry captured by AEDevLens.
 *
 * This is a simple data holder. Parsing and classification logic
 * are provided via extension properties in LogClassifier.
 */
public data class LogEntry(
    val id: String = "log_${Clock.System.now().toEpochMilliseconds()}_${kotlin.random.Random.nextInt(10000)}",
    val severity: LogSeverity,
    val tag: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
)
