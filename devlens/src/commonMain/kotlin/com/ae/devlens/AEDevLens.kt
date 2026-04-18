package com.ae.devlens

import com.ae.devlens.core.DevLensConfig
import com.ae.devlens.core.DevLensPlugin
import com.ae.devlens.core.PluginContext
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.plugins.logs.LogsPlugin
import com.ae.devlens.plugins.logs.model.LogSeverity
import com.ae.devlens.plugins.logs.store.LogStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

/**
 * AEDevLens — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * Instance-based design: testable, supports multiple instances, no hidden globals.
 *
 * ## Quick Start
 * ```kotlin
 * // Use the convenient default instance
 * val inspector = AEDevLens.default
 *
 * // Or create a custom instance
 * val inspector = AEDevLens.create(DevLensConfig(maxLogEntries = 1000))
 * ```
 *
 * ## Logging
 * ```kotlin
 * inspector.log(LogSeverity.INFO, "MyTag", "Something happened")
 * ```
 *
 * ## Custom Plugins
 * ```kotlin
 * inspector.install(MyCustomPlugin())
 * ```
 */
public class AEDevLens private constructor(
    public val config: DevLensConfig,
) {
    private val _plugins = MutableStateFlow<List<DevLensPlugin>>(emptyList())

    /** All registered plugins as a reactive stream */
    public val plugins: StateFlow<List<DevLensPlugin>> = _plugins.asStateFlow()

    /** All UI plugins (plugins that provide a visible tab) */
    public val uiPlugins: List<UIPlugin>
        get() = _plugins.value.filterIsInstance<UIPlugin>()

    /** The built-in log store — shortcut for quick logging */
    public val logStore: LogStore
        get() =
            getPlugin<LogsPlugin>()?.logStore
                ?: error("LogsPlugin is not installed. Install it with inspector.install(LogsPlugin())")

    /**
     * Tracks the CoroutineScope for each registered plugin (keyed by plugin id).
     *
     * Each scope uses [SupervisorJob] so one failing child coroutine doesn't cancel
     * sibling coroutines in the same plugin. Scopes are cancelled on [uninstall].
     */
    private val pluginScopes = mutableMapOf<String, CoroutineScope>()

    init {
        // Install the built-in LogsPlugin by default
        val logsPlugin = LogsPlugin(LogStore(maxEntries = config.maxLogEntries))
        installInternal(logsPlugin)
    }

    // ── Plugin registration ───────────────────────────────────────────────────

    /**
     * Register a plugin with this inspector instance.
     *
     * Duplicate plugin IDs are silently ignored.
     */
    public fun install(plugin: DevLensPlugin) {
        installInternal(plugin)
    }

    private fun installInternal(plugin: DevLensPlugin) {
        var attached = false
        _plugins.update { current ->
            if (current.any { it.id == plugin.id }) {
                current
            } else {
                attached = true
                current + plugin
            }
        }
        if (attached) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            pluginScopes[plugin.id] = scope
            safeCall(plugin.id) { plugin.onAttach(buildContext(scope)) }
        }
    }

    /**
     * Unregister a plugin by its ID.
     *
     * Cancels the plugin's [CoroutineScope] before calling [DevLensPlugin.onDetach].
     */
    public fun uninstall(pluginId: String) {
        var detachedPlugin: DevLensPlugin? = null
        _plugins.update { current ->
            val plugin = current.find { it.id == pluginId }
            if (plugin == null) {
                current
            } else {
                detachedPlugin = plugin
                current.filter { it.id != pluginId }
            }
        }
        detachedPlugin?.let { plugin ->
            // Cancel coroutines BEFORE onDetach so the plugin sees a clean state
            pluginScopes.remove(plugin.id)?.cancel()
            safeCall(pluginId) { plugin.onDetach() }
        }
    }

    // ── Plugin lookup ─────────────────────────────────────────────────────────

    /**
     * Get a plugin by its type.
     *
     * ```kotlin
     * val logsPlugin = inspector.getPlugin<LogsPlugin>()
     * ```
     */
    public inline fun <reified T : DevLensPlugin> getPlugin(): T? =
        plugins.value.filterIsInstance<T>().firstOrNull()

    /** Get a plugin by its ID. */
    public fun getPluginById(id: String): DevLensPlugin? = _plugins.value.find { it.id == id }

    // ── Logging shortcut ──────────────────────────────────────────────────────

    /** Shortcut: log a message to the built-in [LogStore]. */
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
    ) {
        _plugins.value.filterIsInstance<LogsPlugin>().firstOrNull()
            ?.logStore?.log(severity, tag, message)
    }

    // ── Lifecycle notifications ───────────────────────────────────────────────

    /** Notify all plugins the host app has moved to the foreground. */
    internal fun notifyStart() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onStart() }
        }
    }

    /** Notify all plugins the host app has moved to the background. */
    internal fun notifyStop() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onStop() }
        }
    }

    /** Notify all plugins the DevLens UI panel has been opened. */
    internal fun notifyOpen() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onOpen() }
        }
    }

    /** Notify all plugins the DevLens UI panel has been closed. */
    internal fun notifyClose() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onClose() }
        }
    }

    /** Clear all plugin data. */
    public fun clearAll() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onClear() }
        }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /**
     * Builds a [PluginContext] for the given plugin scope.
     *
     * The context exposes only the safe subset of AEDevLens functionality
     * that plugins are allowed to use.
     */
    private fun buildContext(scope: CoroutineScope): PluginContext =
        object : PluginContext {
            override val scope: CoroutineScope = scope
            override val config: DevLensConfig = this@AEDevLens.config
            @Suppress("UNCHECKED_CAST")
            override fun <T : DevLensPlugin> getPlugin(type: KClass<T>): T? =
                _plugins.value.firstOrNull { type.isInstance(it) } as? T
        }

    public companion object {
        /** Convenient default instance for apps that only need one inspector */
        public val default: AEDevLens by lazy { create() }

        /**
         * Create a new AEDevLens instance with custom configuration.
         *
         * Use this for testing or when you need multiple isolated instances.
         */
        public fun create(config: DevLensConfig = DevLensConfig()): AEDevLens = AEDevLens(config)

        /** Safely call a plugin method, swallowing any exception to protect the host app. */
        internal fun safeCall(
            pluginId: String,
            block: () -> Unit,
        ) {
            runCatching { block() }
                .onFailure { /* TODO: route to error reporter */ }
        }
    }
}
