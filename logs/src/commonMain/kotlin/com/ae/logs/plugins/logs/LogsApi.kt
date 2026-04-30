package com.ae.logs.plugins.logs

import com.ae.logs.plugins.logs.model.LogSeverity
import com.ae.logs.plugins.logs.store.LogStore

/**
 * Public write API for [LogsPlugin].
 *
 * This is the only surface external code should use to send logs to AELogs.
 * It intentionally exposes no read access — UI reads via the plugin's internal store.
 *
 * ```kotlin
 * val api = inspector.getPlugin<LogsPlugin>()?.api
 * api?.log(LogSeverity.INFO, "MyTag", "Something happened")
 * api?.e("MyTag", "Oops", throwable)   // stack trace appended automatically
 * ```
 */
public class LogsApi internal constructor(
    private val store: LogStore,
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
        if (!com.ae.logs.AELogs.isEnabled) return

        val config =
            com.ae.logs.AELogs
                .defaultOrNull()
                ?.config
        if (config != null && !config.isEnabled) return
        if (config != null && severity < config.minSeverity) return

        config?.platformLogSink?.log(severity, tag, message, throwable)

        val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
        store.log(severity, tag, fullMessage)
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
