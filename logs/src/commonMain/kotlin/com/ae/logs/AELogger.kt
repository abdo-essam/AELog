package com.ae.logs

import com.ae.logs.plugins.logs.TaggedLogger
import com.ae.logs.plugins.logs.logs

/**
 * Secondary logging API for AELogs. (Consider using [TaggedLogger] as your primary pattern).
 *
 * A thin façade over the [AELogs] instance, modelled after
 * Android's built-in `android.util.Log` class.
 *
 * All methods are **silent no-ops** if [AELogs.init] has not been called yet,
 * so it is safe to call from shared modules that run before app startup.
 *
 * ## Direct logging — tag + message
 *
 * ```kotlin
 * AELogger.v("Auth", "Token checked")
 * AELogger.d("Auth", "Token refreshed")
 * AELogger.i("Auth", "User signed in")
 * AELogger.w("Auth", "Session expiring soon")
 * AELogger.e("Auth", "Login failed", throwable)   // stack trace auto-appended
 * AELogger.wtf("Auth", "Unexpected state")
 * ```
 *
 * ## Tagged logger — eliminate tag repetition (recommended)
 *
 * Create a [TaggedLogger] once per class and omit the tag on every call:
 *
 * ```kotlin
 * class AuthViewModel {
 *     private val log = AELogger.logger("AuthViewModel")
 *
 *     fun login() {
 *         log.d("Login started")
 *         log.e("Failed", throwable)
 *     }
 * }
 * ```
 */
public object AELogger {
    // ── Direct shorthands — tag + message ─────────────────────────────────────

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.VERBOSE] message. */
    public fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.VERBOSE, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.DEBUG] message. */
    public fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.DEBUG, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.INFO] message. */
    public fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.INFO, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.WARN] message. */
    public fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.WARN, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.ERROR] message. */
    public fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.ERROR, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.ASSERT] ("What a Terrible Failure") message. */
    public fun wtf(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.ASSERT, tag, message, throwable)
    }

    // ── Tagged logger factory ─────────────────────────────────────────────────

    /**
     * Create a [TaggedLogger] pre-bound to [tag].
     *
     * ```kotlin
     * private val log = AELogger.logger("AuthViewModel")
     * log.d("Login started")
     * log.e("Failed", throwable)
     * ```
     */
    public fun logger(tag: String): TaggedLogger = TaggedLogger(tag)
}
