package com.ae.log

import com.ae.log.core.AELogPlugin
import com.ae.log.core.bus.EventBus
import com.ae.log.plugins.log.log
import kotlinx.atomicfu.atomic

/**
 * AELog — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * The main entry point to the SDK. Coordinates three sub-systems:
 *
 * ```
 * AELog
 * ├── PluginManager  — registration, lifecycle & scope management
 * ├── EventBus       — cross-plugin pub/sub
 * └── AELogConfig  — global configuration
 * ```
 *
 * ## 1. Setup — single entry point
 * ```kotlin
 * // In Application.onCreate()
 * AELog.init(LogPlugin(), NetworkPlugin(), AnalyticsPlugin())
 * ```
 *
 * ## 2. Log — primary API ([AELog] object)
 *
 * The recommended way to log. [AELog] is a discoverable object modelled after
 * Android's built-in `Log` class — just type `AELog.` and the IDE lists every
 * method:
 *
 * ```kotlin
 * AELog.d("Auth", "Token refreshed")
 * AELog.e("Network", "Request failed", throwable)
 * ```
 *
 * ## 3. Log — tagged logger (eliminates tag repetition)
 *
 * Create one logger per class via [AELog.logger]:
 *
 * ```kotlin
 * class AuthViewModel {
 *     private val log = AELog.logger("AuthViewModel")
 *
 *     fun login() {
 *         log.d("Login started")          // tag is baked in
 *         log.e("Failed", throwable)       // tag is baked in
 *     }
 * }
 * ```
 *
 * All logging calls are **silent no-ops** if [init] has not been called yet.
 *
 * ## 5. App lifecycle integration
 * ```kotlin
 * AELog.default.notifyStart()   // call from onStart()
 * AELog.default.notifyStop()    // call from onStop()
 * ```
 *
 * ## Advanced — accessing plugin APIs directly
 * ```kotlin
 * val networkApi = AELog.plugin<NetworkPlugin>()?.api
 * ```
 */
public class AELog private constructor(
    public val config: AELogConfig,
) {
    // ── Sub-systems ───────────────────────────────────────────────────────────

    public val eventBus: EventBus = EventBus()

    /**
     * Manages plugin registration, lookup, and lifecycle.
     */
    public val plugins: AELogPluginManager = AELogPluginManager(config, eventBus)

    /**
     * Manages app and UI lifecycle notifications to all installed plugins.
     */
    public val lifecycle: AELogLifecycle = AELogLifecycle(plugins, eventBus)

    // ── Companion (factory & singleton) ──────────────────────────────────────

    public companion object {
        private val _default = atomic<AELog?>(null)

        /**
         * The shared default [AELog] instance.
         *
         * Requires [init] to have been called first — throws [IllegalStateException]
         * with a clear message if accessed before initialisation.
         *
         * ```kotlin
         * // Always call init first:
         * AELog.init(LogPlugin())
         *
         * // Then access the instance anywhere:
         * val instance = AELog.default
         * ```
         */
        public val default: AELog
            get() =
                _default.value ?: error(
                    "AELog has not been initialised. " +
                        "Call AELog.init() in Application.onCreate() before accessing AELog.default.",
                )

        /**
         * Null-safe internal accessor used by log extensions so they silently
         * no-op if [init] has not been called yet — consistent with how
         * Timber and similar libraries behave before a tree is planted.
         */
        @PublishedApi
        internal fun defaultOrNull(): AELog? = _default.value

        /**
         * Initialise AELog and configure the shared [default] instance.
         *
         * **Idempotent** — safe to call multiple times; only the first call
         * creates and configures the instance. Subsequent calls return the
         * already-initialised [default] immediately.
         *
         * ```kotlin
         * // Zero-config
         * AELog.init()
         *
         * // With plugins
         * AELog.init(LogPlugin(), NetworkPlugin(), AnalyticsPlugin())
         *
         * // With custom config
         * AELog.init(LogPlugin(), config = AELogConfig())
         * ```
         *
         * @param plugins  Plugins to install on the shared instance.
         * @param config   Core configuration (only applied on first call).
         * @return The shared [default] instance.
         */
        public fun init(
            vararg plugins: AELogPlugin,
            config: AELogConfig = AELogConfig(),
        ): AELog {
            // Fast path: already initialised
            _default.value?.let { return it }

            val instance = AELog(config)

            // CAS guarantees only one winner on concurrent calls; the loser
            // discards its instance and returns the already-set singleton.
            if (_default.compareAndSet(null, instance)) {
                plugins.forEach { instance.plugins.install(it) }
                return instance
            } else {
                return _default.value!!
            }
        }

        /**
         * Global toggle to enable or disable logging.
         * If `false`, all `AELog.*` calls become silent no-ops, and network interceptors bypass recording.
         * Default: `true`.
         */
        public var isEnabled: Boolean = true

        /**
         * Export data from all installed plugins as a formatted string.
         * Useful for attaching dev logs to crash reports.
         */
        public fun export(): String {
            val sb = StringBuilder()
            defaultOrNull()?.plugins?.plugins?.value?.forEach { plugin ->
                val exportedData = plugin.export()
                if (exportedData.isNotBlank()) {
                    sb.append("--- ${plugin.name} ---\n")
                    sb.append(exportedData)
                    sb.append("\n\n")
                }
            }
            return sb.toString().trim()
        }

        /**
         * Look up a plugin on the [default] instance by type.
         *
         * Returns `null` if [init] has not been called or if the plugin
         * is not installed — never throws.
         *
         * ```kotlin
         * val networkApi = AELog.plugin<NetworkPlugin>()?.api
         * ```
         */
        public inline fun <reified T : AELogPlugin> plugin(): T? = defaultOrNull()?.plugins?.getPlugin(T::class)

        /**
         * Create a new **isolated** [AELog] instance with custom configuration.
         *
         * Use for advanced scenarios (e.g. tests, embedded SDKs) where a
         * separate instance is required. For the common case, prefer [init]
         * which configures the shared singleton.
         */
        internal fun create(config: AELogConfig = AELogConfig()): AELog = AELog(config)

        // ── Direct shorthands — tag + message ─────────────────────────────────────

        /** Log a [com.ae.log.plugins.log.model.LogSeverity.VERBOSE] message. */
        public fun v(
            tag: String,
            message: String,
            throwable: Throwable? = null,
        ) {
            log?.log(com.ae.log.plugins.log.model.LogSeverity.VERBOSE, tag, message, throwable)
        }

        /** Log a [com.ae.log.plugins.log.model.LogSeverity.DEBUG] message. */
        public fun d(
            tag: String,
            message: String,
            throwable: Throwable? = null,
        ) {
            log?.log(com.ae.log.plugins.log.model.LogSeverity.DEBUG, tag, message, throwable)
        }

        /** Log a [com.ae.log.plugins.log.model.LogSeverity.INFO] message. */
        public fun i(
            tag: String,
            message: String,
            throwable: Throwable? = null,
        ) {
            log?.log(com.ae.log.plugins.log.model.LogSeverity.INFO, tag, message, throwable)
        }

        /** Log a [com.ae.log.plugins.log.model.LogSeverity.WARN] message. */
        public fun w(
            tag: String,
            message: String,
            throwable: Throwable? = null,
        ) {
            log?.log(com.ae.log.plugins.log.model.LogSeverity.WARN, tag, message, throwable)
        }

        /** Log a [com.ae.log.plugins.log.model.LogSeverity.ERROR] message. */
        public fun e(
            tag: String,
            message: String,
            throwable: Throwable? = null,
        ) {
            log?.log(com.ae.log.plugins.log.model.LogSeverity.ERROR, tag, message, throwable)
        }

        /** Log a [com.ae.log.plugins.log.model.LogSeverity.ASSERT] ("What a Terrible Failure") message. */
        public fun wtf(
            tag: String,
            message: String,
            throwable: Throwable? = null,
        ) {
            log?.log(com.ae.log.plugins.log.model.LogSeverity.ASSERT, tag, message, throwable)
        }

        // ── Tagged logger factory ─────────────────────────────────────────────────

        /**
         * Create a [com.ae.log.plugins.log.TaggedLogger] pre-bound to [tag].
         *
         * ```kotlin
         * private val log = AELog.logger("AuthViewModel")
         * log.d("Login started")
         * log.e("Failed", throwable)
         * ```
         */
        public fun logger(tag: String): com.ae.log.plugins.log.TaggedLogger =
            com.ae.log.plugins.log
                .TaggedLogger(tag)
    }
}
