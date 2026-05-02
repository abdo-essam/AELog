package com.ae.logs

import com.ae.logs.core.AELogsPlugin
import com.ae.logs.core.bus.EventBus
import kotlinx.atomicfu.atomic

/**
 * AELogs — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * The main entry point to the SDK. Coordinates three sub-systems:
 *
 * ```
 * AELogs
 * ├── PluginManager  — registration, lifecycle & scope management
 * ├── EventBus       — cross-plugin pub/sub
 * └── AELogsConfig  — global configuration
 * ```
 *
 * ## 1. Setup — single entry point
 * ```kotlin
 * // In Application.onCreate()
 * AELogs.init(LogsPlugin(), NetworkPlugin(), AnalyticsPlugin())
 * ```
 *
 * ## 2. Log — primary API ([AELogger] object)
 *
 * The recommended way to log. [AELogger] is a discoverable object modelled after
 * Android's built-in `Log` class — just type `AELogger.` and the IDE lists every
 * method:
 *
 * ```kotlin
 * AELogger.d("Auth", "Token refreshed")
 * AELogger.e("Network", "Request failed", throwable)
 * ```
 *
 * ## 3. Log — tagged logger (eliminates tag repetition)
 *
 * Create one logger per class via [AELogger.logger]:
 *
 * ```kotlin
 * class AuthViewModel {
 *     private val log = AELogger.logger("AuthViewModel")
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
 * AELogs.default.notifyStart()   // call from onStart()
 * AELogs.default.notifyStop()    // call from onStop()
 * ```
 *
 * ## Advanced — accessing plugin APIs directly
 * ```kotlin
 * val networkApi = AELogs.plugin<NetworkPlugin>()?.api
 * ```
 */
public class AELogs private constructor(
    public val config: AELogsConfig,
) {
    // ── Sub-systems ───────────────────────────────────────────────────────────

    public val eventBus: EventBus = EventBus()

    /**
     * Manages plugin registration, lookup, and lifecycle.
     */
    public val plugins: AELogsPluginManager = AELogsPluginManager(config, eventBus)

    /**
     * Manages app and UI lifecycle notifications to all installed plugins.
     */
    public val lifecycle: AELogsLifecycle = AELogsLifecycle(plugins, eventBus)

    // ── Companion (factory & singleton) ──────────────────────────────────────

    public companion object {
        private val _default = atomic<AELogs?>(null)

        /**
         * The shared default [AELogs] instance.
         *
         * Requires [init] to have been called first — throws [IllegalStateException]
         * with a clear message if accessed before initialisation.
         *
         * ```kotlin
         * // Always call init first:
         * AELogs.init(LogsPlugin())
         *
         * // Then access the instance anywhere:
         * val instance = AELogs.default
         * ```
         */
        public val default: AELogs
            get() =
                _default.value ?: error(
                    "AELogs has not been initialised. " +
                        "Call AELogs.init() in Application.onCreate() before accessing AELogs.default.",
                )

        /**
         * Null-safe internal accessor used by log extensions so they silently
         * no-op if [init] has not been called yet — consistent with how
         * Timber and similar libraries behave before a tree is planted.
         */
        @PublishedApi
        internal fun defaultOrNull(): AELogs? = _default.value

        /**
         * Initialise AELogs and configure the shared [default] instance.
         *
         * **Idempotent** — safe to call multiple times; only the first call
         * creates and configures the instance. Subsequent calls return the
         * already-initialised [default] immediately.
         *
         * ```kotlin
         * // Zero-config
         * AELogs.init()
         *
         * // With plugins
         * AELogs.init(LogsPlugin(), NetworkPlugin(), AnalyticsPlugin())
         *
         * // With custom config
         * AELogs.init(LogsPlugin(), config = AELogsConfig())
         * ```
         *
         * @param plugins  Plugins to install on the shared instance.
         * @param config   Core configuration (only applied on first call).
         * @return The shared [default] instance.
         */
        public fun init(
            vararg plugins: AELogsPlugin,
            config: AELogsConfig = AELogsConfig(),
        ): AELogs {
            // Fast path: already initialised
            _default.value?.let { return it }

            val instance = AELogs(config)

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
         * If `false`, all `AELogger.*` calls become silent no-ops, and network interceptors bypass recording.
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
         * val networkApi = AELogs.plugin<NetworkPlugin>()?.api
         * ```
         */
        public inline fun <reified T : AELogsPlugin> plugin(): T? = defaultOrNull()?.plugins?.getPlugin(T::class)

        /**
         * Create a new **isolated** [AELogs] instance with custom configuration.
         *
         * Use for advanced scenarios (e.g. tests, embedded SDKs) where a
         * separate instance is required. For the common case, prefer [init]
         * which configures the shared singleton.
         */
        internal fun create(config: AELogsConfig = AELogsConfig()): AELogs = AELogs(config)
    }
}
