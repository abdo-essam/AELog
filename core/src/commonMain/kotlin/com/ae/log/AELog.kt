package com.ae.log

import com.ae.log.config.LogConfig
import com.ae.log.event.EventBus
import com.ae.log.plugin.Lifecycle
import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginManager
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.jvm.JvmStatic

public object AELog {
    @PublishedApi
    internal val instanceAtomic: AtomicRef<LogInspector?> = atomic<LogInspector?>(null)

    @PublishedApi
    internal val instance: LogInspector? get() = instanceAtomic.value

    public val config: LogConfig? get() = instance?.config

    @JvmStatic
    public fun init(
        vararg plugins: Plugin,
        config: LogConfig = LogConfig(),
    ) {
        // Fast-path: already initialized
        if (instanceAtomic.value != null) return
        // Construct and install only if we win the CAS
        val newInstance = LogInspector(config)
        if (instanceAtomic.compareAndSet(null, newInstance)) {
            plugins.forEach { newInstance.plugins.install(it) }
        }
        // If CAS lost, newInstance is abandoned — its SupervisorJob
        // has no children yet, so it is immediately eligible for GC.
    }

    @JvmStatic public fun export(): String = instance?.export() ?: ""

    @JvmStatic public fun clearAll(): Unit = instance?.clearAll() ?: Unit

    private val enabledAtomic = atomic(true)

    @JvmStatic
    public var isEnabled: Boolean
        get() = enabledAtomic.value
        set(value) {
            enabledAtomic.value = value
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
        instanceAtomic.value = null
        enabledAtomic.value = true
    }
}

public class LogInspector internal constructor(
    internal val config: LogConfig,
) {
    internal val eventBus: EventBus = EventBus()

    @PublishedApi
    internal val plugins: PluginManager = PluginManager(config, eventBus)
    internal val lifecycle: Lifecycle = Lifecycle(plugins, eventBus)

    internal fun export(): String {
        val sb = StringBuilder()
        plugins.plugins.value.forEach { p ->
            p.export().takeIf { it.isNotBlank() }?.let { sb.append("--- ${p.name} ---\n$it\n\n") }
        }
        return sb.toString().trim()
    }

    internal fun clearAll(): Unit = lifecycle.clearAll()
}
