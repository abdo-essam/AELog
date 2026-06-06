package com.ae.log

import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginManager
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.jvm.JvmStatic

public object AELog {
    @PublishedApi
    internal val instanceAtomic: AtomicRef<LogInspector?> = atomic(null)

    @PublishedApi
    internal val instance: LogInspector? get() = instanceAtomic.value

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
     * Safe to call from multiple threads concurrently.
     */
    @JvmStatic
    public fun install(plugin: Plugin) {
        // Capture a single stable reference: create if absent, then use that exact instance.
        // Avoids a TOCTOU race on iOS/Native where a second read of instanceAtomic.value
        // could see a stale null after a successful CAS.
        val inspector = instanceAtomic.value
            ?: run {
                val new = LogInspector()
                if (instanceAtomic.compareAndSet(null, new)) new else instanceAtomic.value!!
            }
        inspector.plugins.install(plugin)
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

    private val showNotchStateFlow = MutableStateFlow(true)

    @JvmStatic
    public var showNotch: Boolean
        get() = showNotchStateFlow.value
        set(value) {
            showNotchStateFlow.value = value
        }

    internal val showNotchFlow: kotlinx.coroutines.flow.StateFlow<Boolean> =
        showNotchStateFlow.asStateFlow()

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
        val current = instanceAtomic.value
        if (current != null) {
            current.plugins.uninstallAll()
            current.overlayVisible.value = false
            instanceAtomic.value = null
        }
        enabledStateFlow.value = true
        showNotchStateFlow.value = true
    }
}

@PublishedApi
internal class LogInspector internal constructor() {
    @PublishedApi
    internal val plugins: PluginManager = PluginManager()

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

    internal fun clearAll() = plugins.forEach { it.onClear() }
}
