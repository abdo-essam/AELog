package com.ae.devlens.plugins.logs

import com.ae.devlens.plugins.logs.model.LogSeverity
import com.ae.devlens.plugins.logs.store.LogStore

/**
 * Public write API for [LogsPlugin].
 *
 * This is the only surface external code should use to send logs to DevLens.
 * It intentionally exposes no read access — UI reads via the plugin's internal store.
 *
 * ```kotlin
 * val api = inspector.getPlugin<LogsPlugin>()?.api
 * api?.log(LogSeverity.INFO, "MyTag", "Something happened")
 * ```
 */
public class LogsApi internal constructor(
    private val store: LogStore,
) {
    /**
     * Record a log entry.
     *
     * Safe to call from any thread. Immediately emits to the log viewer.
     */
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
    ) {
        store.log(severity, tag, message)
    }

    /** Convenience shortcut for [LogSeverity.VERBOSE] logs. */
    public fun v(
        tag: String,
        message: String,
    ): Unit = log(LogSeverity.VERBOSE, tag, message)

    /** Convenience shortcut for [LogSeverity.DEBUG] logs. */
    public fun d(
        tag: String,
        message: String,
    ): Unit = log(LogSeverity.DEBUG, tag, message)

    /** Convenience shortcut for [LogSeverity.INFO] logs. */
    public fun i(
        tag: String,
        message: String,
    ): Unit = log(LogSeverity.INFO, tag, message)

    /** Convenience shortcut for [LogSeverity.WARN] logs. */
    public fun w(
        tag: String,
        message: String,
    ): Unit = log(LogSeverity.WARN, tag, message)

    /** Convenience shortcut for [LogSeverity.ERROR] logs. */
    public fun e(
        tag: String,
        message: String,
    ): Unit = log(LogSeverity.ERROR, tag, message)
}
