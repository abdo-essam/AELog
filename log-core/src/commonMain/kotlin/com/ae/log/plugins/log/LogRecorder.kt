@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.log

import com.ae.log.AELog
import com.ae.log.core.utils.IdGenerator
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.plugins.log.model.LogSeverity
import kotlin.time.Clock

public class LogRecorder internal constructor(
    private val store: LogStore,
    private val clock: Clock = Clock.System,
    private val idGenerator: () -> String = { IdGenerator.next() },
) {
    /**
     * Record a log entry.
     *
     * If [throwable] is non-null its stack trace is appended to [message]
     * automatically. Safe to call from any thread.
     */
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        if (!AELog.isEnabled) return

        val config = AELog.instance?.config
        if (config != null && severity.ordinal < config.minSeverity.ordinal) return

        config?.platformLogSink?.log(severity, tag, message, throwable)

        val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message

        store.add(
            LogEntry(
                id = idGenerator(),
                severity = severity,
                tag = tag,
                message = fullMessage,
                timestamp = clock.now().toEpochMilliseconds(),
            ),
        )
    }

    public fun v(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogSeverity.VERBOSE, tag, message, throwable)
    public fun d(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogSeverity.DEBUG, tag, message, throwable)
    public fun i(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogSeverity.INFO, tag, message, throwable)
    public fun w(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogSeverity.WARN, tag, message, throwable)
    public fun e(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogSeverity.ERROR, tag, message, throwable)
    public fun wtf(tag: String, message: String, throwable: Throwable? = null): Unit = log(LogSeverity.ASSERT, tag, message, throwable)
}
