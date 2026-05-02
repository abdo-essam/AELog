@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.log

import com.ae.log.plugins.log.model.LogSeverity
import kotlin.time.Clock

public class LogApi internal constructor(
    private val store: LogStore,
    private val clock: Clock = Clock.System,
    private val idGenerator: () -> String = {
        com.ae.log.core.utils.IdGenerator
            .generateId()
    },
) {
    /**
     * Record a log entry.
     *
     * If [throwable] is non-null its stack trace is appended to [message]
     * automatically — callers never have to format it themselves.
     *
     * Safe to call from any thread. Immediately emits to the log viewer.
     */
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!com.ae.log.AELog.isEnabled) return

        val config =
            com.ae.log.AELog
                .defaultOrNull()
                ?.config
        if (config != null && !config.isEnabled) return
        if (config != null && severity < config.minSeverity) return

        config?.platformLogSink?.log(severity, tag, message, throwable)

        val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        store.add(
            com.ae.log.plugins.log.model.LogEntry(
                id = idGenerator(),
                severity = severity,
                tag = tag,
                message = fullMessage,
                timestamp = clock.now().toEpochMilliseconds(),
            ),
        )
    }

    /** Convenience shortcut for [LogSeverity.VERBOSE] logs. */
    public fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = log(LogSeverity.VERBOSE, tag, message, throwable)

    /** Convenience shortcut for [LogSeverity.DEBUG] logs. */
    public fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = log(LogSeverity.DEBUG, tag, message, throwable)

    /** Convenience shortcut for [LogSeverity.INFO] logs. */
    public fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = log(LogSeverity.INFO, tag, message, throwable)

    /** Convenience shortcut for [LogSeverity.WARN] logs. */
    public fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = log(LogSeverity.WARN, tag, message, throwable)

    /** Convenience shortcut for [LogSeverity.ERROR] logs. */
    public fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = log(LogSeverity.ERROR, tag, message, throwable)

    /** Convenience shortcut for [LogSeverity.ASSERT] ("What a Terrible Failure") logs. */
    public fun wtf(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = log(LogSeverity.ASSERT, tag, message, throwable)
}
