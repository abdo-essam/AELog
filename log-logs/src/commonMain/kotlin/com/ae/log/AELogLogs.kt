package com.ae.log

import com.ae.log.core.utils.callerTag
import com.ae.log.plugins.log.LogPlugin
import com.ae.log.plugins.log.model.LogSeverity
import kotlin.jvm.JvmStatic

/**
 * Extension property to access the standard logging API.
 * Only available if the `:log-logs` module is included.
 */
public val AELog.log: LogProxy get() = LogProxy

public object LogProxy {
    private fun record(
        severity: LogSeverity,
        tag: String,
        msg: String,
        t: Throwable? = null,
    ) {
        AELog.getPlugin<LogPlugin>()?.record(severity, tag, msg, t)
    }

    @JvmStatic public fun v(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.VERBOSE, tag, msg, t)

    @JvmStatic public fun d(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.DEBUG, tag, msg, t)

    @JvmStatic public fun i(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.INFO, tag, msg, t)

    @JvmStatic public fun w(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.WARN, tag, msg, t)

    @JvmStatic public fun e(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.ERROR, tag, msg, t)

    @JvmStatic public fun wtf(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.ASSERT, tag, msg, t)

    @JvmStatic public fun v(
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.VERBOSE, callerTag(), msg, t)

    @JvmStatic public fun d(
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.DEBUG, callerTag(), msg, t)

    @JvmStatic public fun i(
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.INFO, callerTag(), msg, t)

    @JvmStatic public fun w(
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.WARN, callerTag(), msg, t)

    @JvmStatic public fun e(
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.ERROR, callerTag(), msg, t)

    @JvmStatic public fun wtf(
        msg: String,
        t: Throwable? = null,
    ): Unit = record(LogSeverity.ASSERT, callerTag(), msg, t)
}
