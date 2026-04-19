package com.ae.devlens

import com.ae.devlens.plugins.logs.log
import com.ae.devlens.plugins.logs.model.LogSeverity

/**
 * Simple static logging API — works like [Timber](https://github.com/JakeWharton/timber).
 *
 * **Always routes to [AEDevLens.default].** No mutable global state.
 *
 * Install plugins onto [AEDevLens.default] (via [DevLensSetup.init] or manually):
 * ```kotlin
 * DevLensSetup.init()          // recommended
 * // or manually:
 * AEDevLens.default.install(LogsPlugin())
 * ```
 *
 * Then log from anywhere — no reference needed:
 * ```kotlin
 * DevLens.d("MyFeature", "User tapped the button")
 * DevLens.w("Network", "Slow response: 3400ms")
 * DevLens.e("Auth", "Token refresh failed", throwable)
 * ```
 *
 * For a **custom inspector** (non-default), call the extension directly:
 * ```kotlin
 * myInspector.log(LogSeverity.DEBUG, "MyTag", "message")
 * ```
 */
public object DevLens {
    /** Log a [LogSeverity.VERBOSE] message to [AEDevLens.default]. */
    public fun v(
        tag: String,
        message: String,
    ): Unit = AEDevLens.default.log(LogSeverity.VERBOSE, tag, message)

    /** Log a [LogSeverity.DEBUG] message to [AEDevLens.default]. */
    public fun d(
        tag: String,
        message: String,
    ): Unit = AEDevLens.default.log(LogSeverity.DEBUG, tag, message)

    /** Log an [LogSeverity.INFO] message to [AEDevLens.default]. */
    public fun i(
        tag: String,
        message: String,
    ): Unit = AEDevLens.default.log(LogSeverity.INFO, tag, message)

    /** Log a [LogSeverity.WARN] message to [AEDevLens.default]. */
    public fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AEDevLens.default.log(LogSeverity.WARN, tag, message.withThrowable(throwable))

    /** Log an [LogSeverity.ERROR] message to [AEDevLens.default]. */
    public fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AEDevLens.default.log(LogSeverity.ERROR, tag, message.withThrowable(throwable))

    /** Log a [LogSeverity.ASSERT] (fatal) message to [AEDevLens.default]. */
    public fun wtf(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AEDevLens.default.log(LogSeverity.ASSERT, tag, message.withThrowable(throwable))

    private fun String.withThrowable(throwable: Throwable?): String =
        if (throwable != null) "$this\n${throwable.stackTraceToString()}" else this
}
