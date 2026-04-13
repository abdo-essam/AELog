package com.ae.devlens

import com.ae.devlens.core.AEDevLensConfig
import com.ae.devlens.core.DevLensPlugin
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.plugins.logs.LogsPlugin
import com.ae.devlens.plugins.logs.model.LogLevel
import com.ae.devlens.plugins.logs.store.LogStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

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
 * val inspector = AEDevLens.create(AEDevLensConfig(maxLogEntries = 1000))
 * ```
 *
 * ## Logging
 * ```kotlin
 * inspector.log(LogLevel.INFO, "MyTag", "Something happened")
 * ```
 *
 * ## Custom Plugins
 * ```kotlin
 * inspector.install(MyCustomPlugin())
 * ```
 */
public class AEDevLens private constructor(
    public val config: AEDevLensConfig,
) {
    private val _plugins = MutableStateFlow<List<DevLensPlugin>>(emptyList())

    /** All registered plugins */
    public val plugins: StateFlow<List<DevLensPlugin>> = _plugins.asStateFlow()

    /** All UI plugins (plugins that have a visible tab) */
    public val uiPlugins: List<UIPlugin>
        get() = _plugins.value.filterIsInstance<UIPlugin>()

    /** The built-in log store — shortcut for quick logging */
    public val logStore: LogStore
        get() =
            getPlugin<LogsPlugin>()?.logStore
                ?: error("LogsPlugin is not installed. Install it with inspector.install(LogsPlugin())")

    init {
        // Install the built-in LogsPlugin by default
        val logsPlugin = LogsPlugin(LogStore(maxEntries = config.maxLogEntries))
        installInternal(logsPlugin)
    }

    /**
     * Register a plugin with this inspector instance.
     *
     * Duplicate plugin IDs are rejected.
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
            safeCall(plugin.id) { plugin.onAttach(this) }
        }
    }

    /**
     * Unregister a plugin by its ID.
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
            safeCall(pluginId) { plugin.onDetach() }
        }
    }

    /**
     * Get a plugin by its type.
     *
     * ```kotlin
     * val logsPlugin = inspector.getPlugin<LogsPlugin>()
     * ```
     */
    public inline fun <reified T : DevLensPlugin> getPlugin(): T? = plugins.value.filterIsInstance<T>().firstOrNull()

    /**
     * Get a plugin by its ID.
     */
    public fun getPluginById(id: String): DevLensPlugin? = _plugins.value.find { it.id == id }

    /**
     * Shortcut: Log a message to the built-in LogStore.
     */
    public fun log(
        level: LogLevel,
        tag: String,
        message: String,
    ) {
        val logsPlugin = _plugins.value.filterIsInstance<LogsPlugin>().firstOrNull()
        logsPlugin?.logStore?.log(level, tag, message)
    }

    /**
     * Notify all plugins that the DevLens UI has been opened.
     */
    internal fun notifyOpen() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onOpen() }
        }
    }

    /**
     * Notify all plugins that the DevLens UI has been closed.
     */
    internal fun notifyClose() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onClose() }
        }
    }

    /**
     * Clear all plugin data.
     */
    public fun clearAll() {
        _plugins.value.forEach { plugin ->
            safeCall(plugin.id) { plugin.onClear() }
        }
    }

    public companion object {
        /** Convenient default instance for apps that only need one inspector */
        public val default: AEDevLens by lazy { create() }

        /**
         * Create a new AEDevLens instance with custom configuration.
         *
         * Use this for testing or when you need multiple isolated instances.
         */
        public fun create(config: AEDevLensConfig = AEDevLensConfig()): AEDevLens = AEDevLens(config)

        /**
         * Safely call a plugin method, catching and logging any exceptions.
         */
        internal fun safeCall(
            pluginId: String,
            block: () -> Unit,
        ) {
            runCatching { block() }
                .onFailure { e ->
                    // Optionally log via an error handler or ignore if none exists.
                }
        }
    }
}
