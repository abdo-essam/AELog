package com.ae.devlens

import com.ae.devlens.core.DevLensPlugin
import com.ae.devlens.core.bus.AllDataClearedEvent
import com.ae.devlens.core.bus.AppStartedEvent
import com.ae.devlens.core.bus.AppStoppedEvent
import com.ae.devlens.core.bus.EventBus
import com.ae.devlens.core.bus.PanelClosedEvent
import com.ae.devlens.core.bus.PanelOpenedEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * AEDevLens — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * The main entry point to the SDK. Coordinates three sub-systems:
 *
 * ```
 * AEDevLens
 * ├── PluginManager  — registration, lifecycle & scope management
 * ├── EventBus       — cross-plugin pub/sub
 * └── DevLensConfig  — global configuration
 * ```
 *
 * ## Setup (fluent builder style)
 * ```kotlin
 * val inspector = AEDevLens.create(DevLensConfig(maxLogEntries = 1000))
 *     .install(LogsPlugin())
 *     .install(NetworkPlugin())
 *     .install(CrashPlugin())
 * ```
 *
 * ## Simple setup (default instance + auto-install)
 * ```kotlin
 * val inspector = AEDevLens.createDefault()   // from :devlens aggregator
 * ```
 *
 * ## App lifecycle integration
 * ```kotlin
 * inspector.notifyStart()   // call from onStart()
 * inspector.notifyStop()    // call from onStop()
 * ```
 */
public class AEDevLens private constructor(
    public val config: DevLensConfig,
) {

    // ── Sub-systems ───────────────────────────────────────────────────────────

    /**
     * Shared event bus for all plugins on this instance.
     * Also exposed to each plugin via [com.ae.devlens.core.PluginContext.eventBus].
     */
    public val eventBus: EventBus = EventBus()

    /** Internal plugin lifecycle manager. */
    private val pluginManager = PluginManager(config, eventBus)

    /** Hot stream of all currently registered plugins. */
    public val plugins: StateFlow<List<DevLensPlugin>> = pluginManager.plugins

    // ── Fluent plugin registration ────────────────────────────────────────────

    /**
     * Install a plugin and return **this** instance for chaining.
     *
     * Duplicate plugin IDs are silently ignored (idempotent).
     * [com.ae.devlens.core.DevLensPlugin.onAttach] is called synchronously.
     *
     * ```kotlin
     * AEDevLens.create()
     *     .install(LogsPlugin())
     *     .install(NetworkPlugin())
     *     .install(CrashPlugin())
     * ```
     */
    public fun install(plugin: DevLensPlugin): AEDevLens {
        pluginManager.install(plugin)
        return this
    }

    /**
     * Uninstall a plugin by ID and return **this** instance for chaining.
     *
     * The plugin's [com.ae.devlens.core.PluginContext.scope] is cancelled
     * before [com.ae.devlens.core.DevLensPlugin.onDetach] is called.
     */
    public fun uninstall(pluginId: String): AEDevLens {
        pluginManager.uninstall(pluginId)
        return this
    }

    // ── Plugin lookup ─────────────────────────────────────────────────────────

    /**
     * Get a registered plugin by type. Returns `null` if not installed.
     *
     * ```kotlin
     * val logs: LogsPlugin? = inspector.getPlugin<LogsPlugin>()
     * ```
     */
    public inline fun <reified T : DevLensPlugin> getPlugin(): T? =
        plugins.value.filterIsInstance<T>().firstOrNull()

    /** Get a registered plugin by its stable string ID. */
    public fun getPluginById(id: String): DevLensPlugin? =
        pluginManager.getPluginById(id)

    // ── Lifecycle notifications ───────────────────────────────────────────────

    /**
     * Notify all plugins the host app has moved to the **foreground**.
     * Publishes [AppStartedEvent] to [eventBus].
     */
    public fun notifyStart(): Unit {
        pluginManager.forEach { it.onStart() }
        eventBus.publish(AppStartedEvent)
    }

    /**
     * Notify all plugins the host app has moved to the **background**.
     * Publishes [AppStoppedEvent] to [eventBus].
     */
    public fun notifyStop(): Unit {
        pluginManager.forEach { it.onStop() }
        eventBus.publish(AppStoppedEvent)
    }

    /**
     * Notify all plugins the DevLens UI panel has been **opened**.
     * Publishes [PanelOpenedEvent] to [eventBus].
     */
    public fun notifyOpen(): Unit {
        pluginManager.forEach { it.onOpen() }
        eventBus.publish(PanelOpenedEvent)
    }

    /**
     * Notify all plugins the DevLens UI panel has been **closed**.
     * Publishes [PanelClosedEvent] to [eventBus].
     */
    public fun notifyClose(): Unit {
        pluginManager.forEach { it.onClose() }
        eventBus.publish(PanelClosedEvent)
    }

    /** Clear all plugin data and publish [AllDataClearedEvent]. */
    public fun clearAll(): Unit {
        pluginManager.forEach { it.onClear() }
        eventBus.publish(AllDataClearedEvent)
    }

    // ── Companion (factory) ───────────────────────────────────────────────────

    public companion object {

        /**
         * Shared default instance for apps that only need one inspector.
         *
         * Call [install] on this instance on app startup, or use
         * `AEDevLens.createDefault()` from the `:devlens` aggregator for
         * a pre-configured instance with [LogsPlugin] included.
         */
        public val default: AEDevLens by lazy { create() }

        /**
         * Create a new isolated [AEDevLens] instance with custom configuration.
         *
         * Prefer the fluent style:
         * ```kotlin
         * val inspector = AEDevLens.create(DevLensConfig(maxLogEntries = 1000))
         *     .install(LogsPlugin())
         * ```
         */
        public fun create(config: DevLensConfig = DevLensConfig()): AEDevLens =
            AEDevLens(config)
    }
}
