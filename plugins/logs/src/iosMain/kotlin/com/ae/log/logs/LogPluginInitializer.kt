package com.ae.log.logs

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Logs plugin on iOS.
 *
 * Uses [@EagerInitialization][kotlin.native.EagerInitialization] so this property is evaluated
 * when the Kotlin/Native framework is loaded — before any user code runs — registering
 * [LogPlugin] with AELog automatically (zero-config, same as the Android ContentProvider initializer).
 *
 * ## Zero-config usage
 * Just add the dependency — no `AELog.configure { }` call required:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:ae-log-logs:<version>")
 * ```
 *
 * ## Opt-out / custom config
 * Call `AELog.configure { plugin(LogPlugin(maxEntries = 1_000)) }` yourself and the
 * idempotent install guard will skip the eager-init instance.
 */
@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@Suppress("DEPRECATION") // @EagerInitialization is the only zero-config iOS init mechanism.
@EagerInitialization
private val initLogPlugin = run {
    AELog.install(LogPlugin())
}
