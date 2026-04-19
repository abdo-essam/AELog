package com.ae.devlens.core

import com.ae.devlens.DevLensConfig
import com.ae.devlens.core.bus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/**
 * Scoped context passed to each plugin on [DevLensPlugin.onAttach].
 *
 * Provides a controlled, minimal API surface — plugins only get what they need,
 * not a reference to the full [com.ae.devlens.AEDevLens] instance.
 *
 * ## What plugins CAN do via context
 * - Launch coroutines safely via [scope] (auto-cancelled before [DevLensPlugin.onDetach])
 * - Read global config via [config]
 * - Publish / subscribe to events via [eventBus]
 * - Look up sibling plugins via [getPlugin]
 *
 * ## What plugins CANNOT do
 * - Install or uninstall other plugins
 * - Show / hide the DevLens overlay
 * - Access internal lifecycle machinery
 */
public interface PluginContext {
    /**
     * CoroutineScope tied to this plugin's lifetime.
     *
     * All coroutines launched here are cancelled automatically when the plugin is
     * detached — no manual cleanup required. Uses [kotlinx.coroutines.SupervisorJob]
     * so one failing child doesn't cancel sibling coroutines.
     */
    public val scope: CoroutineScope

    /** Read-only view of the global DevLens configuration. */
    public val config: DevLensConfig

    /**
     * Shared event bus for cross-plugin communication.
     *
     * Publish custom events or subscribe to events from other plugins.
     * Built-in system events ([com.ae.devlens.core.bus.PanelOpenedEvent], etc.)
     * are published automatically by DevLens itself.
     */
    public val eventBus: EventBus

    /**
     * Look up a sibling plugin by type.
     *
     * Returns `null` if the plugin is not installed.
     *
     * ```kotlin
     * val logs = context.getPlugin<LogsPlugin>()
     * ```
     */
    public fun <T : DevLensPlugin> getPlugin(type: KClass<T>): T?
}

/**
 * Kotlin reified convenience wrapper for [PluginContext.getPlugin].
 *
 * ```kotlin
 * val logs = context.getPlugin<LogsPlugin>()
 * ```
 */
public inline fun <reified T : DevLensPlugin> PluginContext.getPlugin(): T? = getPlugin(T::class)
