package com.ae.log

import com.ae.log.config.LogConfig
import com.ae.log.event.EventBus
import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginManager
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.jvm.JvmStatic

public object AELog {
    @PublishedApi
    internal val instanceAtomic: AtomicRef<LogInspector?> = atomic(null)

    @PublishedApi
    internal val instance: LogInspector? get() = instanceAtomic.value

    internal val config: LogConfig? get() = instance?.config

    /**
     * Configures the AELog library and its plugins using a type-safe Kotlin DSL.
     *
     * ## Usage
     * ```kotlin
     * AELog.configure {
     *     // Core configuration (optional)
     *     enabled = true
     *     dispatcher = Dispatchers.Default
     *     errorHandler = { t -> println("SDK Error: ${t.message}") }
     *
     *     // Override or install plugins
     *     plugin(LogPlugin(maxEntries = 2_000))
     *     plugin(NetworkPlugin(maxEntries = 500))
     * }
     * ```
     */
    @JvmStatic
    public fun configure(block: AELogBuilder.() -> Unit) {
        val builder = AELogBuilder().apply(block)
        if (instanceAtomic.value == null) {
            val newInstance = builder.build()
            instanceAtomic.compareAndSet(null, newInstance)
        } else {
            val inspector = instanceAtomic.value!!
            builder.plugins.distinctBy { it.id }.forEach { plugin ->
                val oldPlugin = inspector.plugins.getPluginById(plugin.id)
                if (oldPlugin != null) {
                    plugin.onMigrateFrom(oldPlugin)
                }
                inspector.plugins.uninstall(plugin.id)
                inspector.plugins.install(plugin)
            }
        }
    }



    /**
     * Installs a new plugin into AELog.
     *
     * Use this to add a brand-new feature or custom plugin that doesn't exist
     * in AELog by default — for example, a custom database viewer or live
     * metrics tab:
     * ```kotlin
     * AELog.install(MyDatabasePlugin())
     * AELog.install(LiveMetricsPlugin())
     * ```
     *
     * To **reconfigure** an existing built-in plugin (e.g. change max entries),
     * use [override] instead.
     *
     * Safe to call from multiple threads concurrently.
     */
    @JvmStatic
    public fun install(plugin: Plugin) {
        if (instanceAtomic.value == null) {
            val newInstance = LogInspector(LogConfig())
            instanceAtomic.compareAndSet(null, newInstance)
        }
        instanceAtomic.value?.plugins?.install(plugin)
    }

    @JvmStatic public fun export(): String = instance?.export() ?: ""

    @JvmStatic public fun clearAll(): Unit = instance?.clearAll() ?: Unit

    private val enabledStateFlow = MutableStateFlow(true)

    @JvmStatic
    public var isEnabled: Boolean
        get() = enabledStateFlow.value
        set(value) {
            enabledStateFlow.value = value
        }

    internal val isEnabledFlow: kotlinx.coroutines.flow.StateFlow<Boolean> =
        enabledStateFlow.asStateFlow()

    /**
     * Programmatically opens the AELog overlay panel.
     *
     * Can be called from anywhere — no composable context needed.
     * Has no effect if no [AELogOverlay] is active in the UI.
     *
     * ```kotlin
     * // From a shake gesture, debug menu, etc.
     * AELog.show()
     * ```
     */
    @JvmStatic
    public fun show() {
        instance?.overlayVisible?.value = true
    }

    /**
     * Programmatically closes the AELog overlay panel.
     */
    @JvmStatic
    public fun hide() {
        instance?.overlayVisible?.value = false
    }

    public inline fun <reified T : Plugin> getPlugin(): T? = instance?.plugins?.getPlugin(T::class)

    /**
     * Resets the singleton to an uninitialised state.
     *
     * **For use in unit tests only.** On Android, the process lifecycle handles
     * cleanup — this method is not needed in production code.
     *
     * Cancels all active plugin coroutine scopes before clearing the reference,
     * ensuring no coroutines leak between tests.
     */
    @AELogTestApi
    public fun resetForTesting() {
        val current = instanceAtomic.value ?: return
        current.lifecycle.notifyStop()
        current.plugins.uninstallAll()
        current.overlayVisible.value = false
        instanceAtomic.value = null
        enabledStateFlow.value = true
    }
}

@PublishedApi
internal class LogInspector internal constructor(
    internal val config: LogConfig,
) {
    internal val eventBus: EventBus = EventBus()

    @PublishedApi
    internal val plugins: PluginManager = PluginManager(config, eventBus)
    internal val lifecycle: AELogLifecycle = AELogLifecycle(plugins, eventBus)

    /**
     * Backing state for the overlay panel visibility.
     *
     * Written by [AELog.show] / [AELog.hide] and observed by
     * [com.ae.log.ui.AELogOverlay] via [LogController].
     */
    internal val overlayVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)

    internal fun export(): String {
        val sb = StringBuilder()
        plugins.plugins.value.forEach { p ->
            p.export().takeIf { it.isNotBlank() }?.let { sb.append("--- ${p.name} ---\n$it\n\n") }
        }
        return sb.toString().trim()
    }

    internal fun clearAll(): Unit = lifecycle.clearAll()
}

public class AELogBuilder {
    public var enabled: Boolean = true
    public var dispatcher: CoroutineDispatcher = Dispatchers.Default
    public var errorHandler: (Throwable) -> Unit = { t -> println("[AELog] Internal error: ${t.message}") }
    public var showNotch: Boolean = true

    @PublishedApi
    internal val plugins: MutableList<Plugin> = mutableListOf()

    public fun plugin(plugin: Plugin) {
        plugins.add(plugin)
    }

    internal fun build(): LogInspector {
        val config = LogConfig(
            enabled = enabled,
            dispatcher = dispatcher,
            errorHandler = errorHandler,
            showNotch = showNotch
        )
        val inspector = LogInspector(config)
        plugins.forEach { plugin ->
            inspector.plugins.install(plugin)
        }
        return inspector
    }
}

