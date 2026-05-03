package com.ae.log

import com.ae.log.core.Plugin
import com.ae.log.core.bus.EventBus
import com.ae.log.core.utils.callerTag
import com.ae.log.plugins.log.LogPlugin
import com.ae.log.plugins.log.model.LogSeverity
import kotlinx.atomicfu.atomic
import kotlin.jvm.JvmStatic

/**
 * AELog — Extensible on-device dev tools for Kotlin Multiplatform.
 *
 * Use the namespaced properties to access different recording domains:
 * - [log] for standard debug logging.
 * - [network] for HTTP traffic (via the Network module).
 * - [analytics] for event tracking (via the Analytics module).
 */
public object AELog {
    private val instanceAtomic = atomic<LogInspector?>(null)

    @PublishedApi
    internal val instance: LogInspector? get() = instanceAtomic.value

    /**
     * Initialise the shared AELog instance.
     *
     * @param plugins Plugins to install (LogPlugin, NetworkPlugin, etc.)
     * @param config Global configuration.
     */
    @JvmStatic
    public fun init(
        vararg plugins: Plugin,
        config: LogConfig = LogConfig(),
    ) {
        instanceAtomic.value?.let { return }
        val newInstance = LogInspector(config)
        if (instanceAtomic.compareAndSet(null, newInstance)) {
            plugins.forEach { newInstance.plugins.install(it) }
        }
    }

    /** Access core logging tools. */
    public val log: LogProxy get() = LogProxy

    /** Export all recorded data from all plugins as a string. */
    @JvmStatic
    public fun export(): String = instance?.export() ?: ""

    /** Clear all data from all plugins. */
    @JvmStatic
    public fun clearAll(): Unit = instance?.clearAll() ?: Unit

    private val enabledAtomic = atomic(true)

    /** Internal Kill-switch. */
    @JvmStatic
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    internal var isEnabled: Boolean
        get() = enabledAtomic.value
        set(value) { enabledAtomic.value = value }

    /** Internal lookup. */
    @PublishedApi
    @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
    internal inline fun <reified T : Plugin> getPlugin(): T? =
        instance?.plugins?.getPlugin(T::class)

    @PublishedApi
    internal fun record(severity: LogSeverity, tag: String, msg: String, t: Throwable?) {
        instance?.record(severity, tag, msg, t)
    }
}

/**
 * Proxy for core logging actions.
 */
public object LogProxy {
    /** Generic log entry point. */
    @JvmStatic
    public fun entry(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    ) {
        AELog.record(severity, tag, message, throwable)
    }

    @JvmStatic
    public fun v(tag: String, msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.VERBOSE, tag, msg, t)
    @JvmStatic
    public fun d(tag: String, msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.DEBUG, tag, msg, t)
    @JvmStatic
    public fun i(tag: String, msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.INFO, tag, msg, t)
    @JvmStatic
    public fun w(tag: String, msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.WARN, tag, msg, t)
    @JvmStatic
    public fun e(tag: String, msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.ERROR, tag, msg, t)
    @JvmStatic
    public fun wtf(tag: String, msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.ASSERT, tag, msg, t)

    @JvmStatic
    public fun v(msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.VERBOSE, callerTag(), msg, t)
    @JvmStatic
    public fun d(msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.DEBUG, callerTag(), msg, t)
    @JvmStatic
    public fun i(msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.INFO, callerTag(), msg, t)
    @JvmStatic
    public fun w(msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.WARN, callerTag(), msg, t)
    @JvmStatic
    public fun e(msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.ERROR, callerTag(), msg, t)
    @JvmStatic
    public fun wtf(msg: String, t: Throwable? = null): Unit = AELog.record(LogSeverity.ASSERT, callerTag(), msg, t)
}

/**
 * The actual engine behind AELog. Holds plugins, event bus, and state.
 */
public class LogInspector internal constructor(
    internal val config: LogConfig,
) {
    internal val eventBus: EventBus = EventBus()
    @PublishedApi
    internal val plugins: PluginManager = PluginManager(config, eventBus)
    internal val lifecycle: Lifecycle = Lifecycle(plugins, eventBus)

    internal fun record(severity: LogSeverity, tag: String, msg: String, t: Throwable?) {
        plugins.getPlugin(LogPlugin::class)?.recorder?.log(severity, tag, msg, t)
    }

    internal fun export(): String {
        val sb = StringBuilder()
        plugins.plugins.value.forEach { plugin ->
            val data = plugin.export()
            if (data.isNotBlank()) {
                sb.append("--- ${plugin.name} ---\n$data\n\n")
            }
        }
        return sb.toString().trim()
    }

    internal fun clearAll(): Unit = lifecycle.clearAll()
}
