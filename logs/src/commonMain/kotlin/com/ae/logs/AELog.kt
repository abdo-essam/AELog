package com.ae.logs

import com.ae.logs.plugins.logs.TaggedLogger
import com.ae.logs.plugins.logs.d
import com.ae.logs.plugins.logs.e
import com.ae.logs.plugins.logs.i
import com.ae.logs.plugins.logs.logger
import com.ae.logs.plugins.logs.v
import com.ae.logs.plugins.logs.w
import com.ae.logs.plugins.logs.wtf

/**
 * Primary, zero-ceremony logging API for AELogs.
 *
 * A thin façade over the [AELogs] companion extensions, modelled after
 * Android's built-in `android.util.Log` class — named `AELog` (singular)
 * to avoid import conflicts with that class.
 *
 * All methods are **silent no-ops** if [AELogs.init] has not been called yet,
 * so it is safe to call from shared modules that run before app startup.
 *
 * ## Direct logging — tag + message
 *
 * ```kotlin
 * AELog.v("Auth", "Token checked")
 * AELog.d("Auth", "Token refreshed")
 * AELog.i("Auth", "User signed in")
 * AELog.w("Auth", "Session expiring soon")
 * AELog.e("Auth", "Login failed", throwable)   // stack trace auto-appended
 * AELog.wtf("Auth", "Unexpected state")
 * ```
 *
 * ## Tagged logger — eliminate tag repetition (recommended)
 *
 * Create a [TaggedLogger] once per class and omit the tag on every call:
 *
 * ```kotlin
 * class AuthViewModel {
 *     private val log = AELog.logger("AuthViewModel")
 *
 *     fun login() {
 *         log.d("Login started")
 *         log.e("Failed", throwable)
 *     }
 * }
 * ```
 */
public object AELog {
    // ── Direct shorthands — tag + message ─────────────────────────────────────

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.VERBOSE] message. */
    public fun v(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AELogs.v(tag, message, throwable)

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.DEBUG] message. */
    public fun d(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AELogs.d(tag, message, throwable)

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.INFO] message. */
    public fun i(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AELogs.i(tag, message, throwable)

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.WARN] message. */
    public fun w(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AELogs.w(tag, message, throwable)

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.ERROR] message. */
    public fun e(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AELogs.e(tag, message, throwable)

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.ASSERT] ("What a Terrible Failure") message. */
    public fun wtf(
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ): Unit = AELogs.wtf(tag, message, throwable)

    // ── Tagged logger factory ─────────────────────────────────────────────────

    /**
     * Create a [TaggedLogger] pre-bound to [tag].
     *
     * ```kotlin
     * private val log = AELog.logger("AuthViewModel")
     * log.d("Login started")
     * log.e("Failed", throwable)
     * ```
     */
    public fun logger(tag: String): TaggedLogger = AELogs.logger(tag)
}
