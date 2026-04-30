package com.ae.logs.plugins.logs

import com.ae.logs.AELogs

/**
 * A pre-tagged logger that eliminates tag repetition on every call.
 *
 * Create one per class via [AELogs.logger] or [com.ae.logs.AELogger.logger]:
 *
 * ```kotlin
 * class AuthViewModel {
 *     private val log = AELogs.logger("AuthViewModel")
 *
 *     fun login() {
 *         log.d("Login started")           // tag is baked in
 *         log.e("Token refresh failed", throwable)
 *     }
 * }
 * ```
 *
 * All methods are **silent no-ops** if [AELogs.init] has not been called yet.
 *
 * @param tag The tag that will be prepended to every log entry.
 */
public class TaggedLogger(
    public val tag: String,
) {
    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.VERBOSE] message. */
    public fun v(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.VERBOSE, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.DEBUG] message. */
    public fun d(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.DEBUG, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.INFO] message. */
    public fun i(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.INFO, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.WARN] message. */
    public fun w(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.WARN, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.ERROR] message. */
    public fun e(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.ERROR, tag, message, throwable)
    }

    /** Log a [com.ae.logs.plugins.logs.model.LogSeverity.ASSERT] ("What a Terrible Failure") message. */
    public fun wtf(
        message: String,
        throwable: Throwable? = null,
    ) {
        AELogs.logs?.log(com.ae.logs.plugins.logs.model.LogSeverity.ASSERT, tag, message, throwable)
    }
}

// ── Factory ───────────────────────────────────────────────────────────────────

/**
 * Creates a [TaggedLogger] pre-bound to [tag].
 *
 * ```kotlin
 * // At class level — tag is set once, used everywhere
 * private val log = AELogs.logger("AuthViewModel")
 *
 * log.d("Login started")
 * log.e("Failed", throwable)
 * ```
 */
public fun AELogs.Companion.logger(tag: String): TaggedLogger = TaggedLogger(tag)
