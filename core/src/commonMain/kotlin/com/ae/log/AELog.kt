package com.ae.log

import com.ae.log.config.LogConfig
import com.ae.log.event.EventBus
import com.ae.log.plugin.Plugin
import com.ae.log.plugin.PluginManager
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.jvm.JvmStatic

public object AELog {
    @PublishedApi
    internal val instanceAtomic: AtomicRef<LogInspector?> = atomic(null)

    @PublishedApi
    internal val instance: LogInspector? get() = instanceAtomic.value

    public val config: LogConfig? get() = instance?.config

    /**
     * Configures AELog with the given plugins and optional [LogConfig].
     *
     * ## Zero-config (auto-init)
     * If every plugin dependency is on the classpath, each plugin's
     * `ContentProvider` auto-registers itself before `Application.onCreate()`.
     * You never need to call this method unless you want **custom configuration**.
     *
     * ## Custom configuration — no manifest changes needed
     * Call this from `Application.onCreate()` with only the plugins you want to
     * reconfigure. Each plugin provided here **replaces** the auto-registered
     * default; plugins not mentioned are left untouched.
     *
     * ```kotlin
     * // Replace only LogPlugin's config — Network/Analytics/Crash keep defaults
     * AELog.configure(LogPlugin(maxEntries = 2_000))
     *
     * // Replace all four
     * AELog.configure(
     *     LogPlugin(maxEntries = 2_000),
     *     NetworkPlugin(maxEntries = 500),
     *     AnalyticsPlugin(maxEntries = 1_000),
     *     CrashPlugin(this),
     * )
     * ```
     *
     * The [config] parameter (dispatcher, errorHandler, etc.) is applied only
     * when AELog has not yet been initialised; if auto-init already ran it is
     * ignored in favour of the existing instance.
     */
    @JvmStatic
    public fun configure(
        vararg plugins: Plugin,
        config: LogConfig = LogConfig(),
    ) {
        // Ensure the core singleton exists. If auto-init (ContentProviders) already
        // created it, we reuse that instance; otherwise we create it now with
        // the provided config.
        if (instanceAtomic.value == null) {
            val newInstance = LogInspector(config)
            instanceAtomic.compareAndSet(null, newInstance)
            // If CAS lost, newInstance is abandoned — no children yet, GC-eligible.
        }
        val inspector = instanceAtomic.value!!
        // Replace each explicitly provided plugin: uninstall the auto-registered
        // default (if any) and install the consumer-configured version in its place.
        // Plugins not mentioned here are left as-is.
        plugins.distinctBy { it.id }.forEach { plugin ->
            val oldPlugin = inspector.plugins.getPluginById(plugin.id)
            if (oldPlugin != null) {
                plugin.onMigrateFrom(oldPlugin)
            }
            inspector.plugins.uninstall(plugin.id)
            inspector.plugins.install(plugin)
        }
    }

    /**
     * Registers a single plugin, lazily initialising the AELog core if needed.
     *
     * **Intended for plugin auto-initializers only.** Each plugin's `ContentProvider`
     * calls this before `Application.onCreate()` so consumers need zero setup code.
     * Application code should prefer [init] when custom configuration is required.
     *
     * Safe to call from multiple ContentProviders concurrently — the first call
     * creates the [LogInspector] singleton (with default [LogConfig]); subsequent
     * calls reuse the same instance.
     */
    @JvmStatic
    public fun registerPlugin(plugin: Plugin) {
        if (instanceAtomic.value == null) {
            val newInstance = LogInspector(LogConfig())
            instanceAtomic.compareAndSet(null, newInstance)
        }
        instanceAtomic.value?.plugins?.install(plugin)
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
    internal val lifecycle: AELogLifecycle = AELogLifecycle(plugins, eventBus)

    internal fun export(): String {
        val sb = StringBuilder()
        plugins.plugins.value.forEach { p ->
            p.export().takeIf { it.isNotBlank() }?.let { sb.append("--- ${p.name} ---\n$it\n\n") }
        }
        return sb.toString().trim()
    }

    internal fun clearAll(): Unit = lifecycle.clearAll()
}
