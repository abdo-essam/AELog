package com.ae.devlens.core

import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

/**
 * Scoped context passed to each plugin on [DevLensPlugin.onAttach].
 *
 * Provides a controlled, minimal API surface — plugins only get what they need,
 * not a reference to the full [com.ae.devlens.AEDevLens] instance.
 *
 * ## What plugins can do via context
 * - Launch coroutines safely via [scope] (cancelled automatically on [DevLensPlugin.onDetach])
 * - Read global config via [config]
 * - Look up sibling plugins via [getPlugin]
 *
 * ## What plugins cannot do
 * - Install/uninstall other plugins
 * - Show/hide the DevLens overlay
 * - Access internal lifecycle machinery
 */
public interface PluginContext {

    /**
     * CoroutineScope tied to this plugin's lifetime.
     *
     * Launched from this scope are automatically cancelled when the plugin is detached.
     * Uses [kotlinx.coroutines.SupervisorJob] so one failing child doesn't cancel others.
     */
    val scope: CoroutineScope

    /** Global DevLens configuration for this instance. */
    val config: DevLensConfig

    /**
     * Look up a sibling plugin by type.
     *
     * ```kotlin
     * val logs = context.getPlugin<LogsPlugin>()
     * ```
     */
    fun <T : DevLensPlugin> getPlugin(type: KClass<T>): T?
}

/**
 * Kotlin reified convenience wrapper for [PluginContext.getPlugin].
 *
 * ```kotlin
 * val logs = context.getPlugin<LogsPlugin>()
 * ```
 */
public inline fun <reified T : DevLensPlugin> PluginContext.getPlugin(): T? =
    getPlugin(T::class)
