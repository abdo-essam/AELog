@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log

import com.ae.log.core.LogRecordSink
import com.ae.log.core.Plugin
import com.ae.log.core.bus.EventBus
import com.ae.log.core.utils.callerTag
import com.ae.log.plugins.log.model.LogSeverity
import kotlinx.atomicfu.AtomicRef
import kotlinx.atomicfu.atomic
import kotlin.jvm.JvmStatic

public object AELog {
    @PublishedApi
    internal val instanceAtomic: AtomicRef<LogInspector?> = atomic<LogInspector?>(null)

    @PublishedApi
    internal val instance: LogInspector? get() = instanceAtomic.value

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

    public val log: LogProxy get() = LogProxy

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

    @PublishedApi
    internal fun record(
        severity: LogSeverity,
        tag: String,
        msg: String,
        t: Throwable?,
    ) {
        instance?.record(severity, tag, msg, t)
    }
}

public object LogProxy {
    @JvmStatic public fun v(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.VERBOSE, tag, msg, t)

    @JvmStatic public fun d(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.DEBUG, tag, msg, t)

    @JvmStatic public fun i(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.INFO, tag, msg, t)

    @JvmStatic public fun w(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.WARN, tag, msg, t)

    @JvmStatic public fun e(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.ERROR, tag, msg, t)

    @JvmStatic public fun wtf(
        tag: String,
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.ASSERT, tag, msg, t)

    @JvmStatic public fun v(
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.VERBOSE, callerTag(), msg, t)

    @JvmStatic public fun d(
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.DEBUG, callerTag(), msg, t)

    @JvmStatic public fun i(
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.INFO, callerTag(), msg, t)

    @JvmStatic public fun w(
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.WARN, callerTag(), msg, t)

    @JvmStatic public fun e(
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.ERROR, callerTag(), msg, t)

    @JvmStatic public fun wtf(
        msg: String,
        t: Throwable? = null,
    ): Unit = AELog.record(LogSeverity.ASSERT, callerTag(), msg, t)
}

public class LogInspector internal constructor(
    internal val config: LogConfig,
) {
    internal val eventBus: EventBus = EventBus()

    @PublishedApi
    internal val plugins: PluginManager = PluginManager(config, eventBus)
    internal val lifecycle: Lifecycle = Lifecycle(plugins, eventBus)

    internal fun record(
        severity: LogSeverity,
        tag: String,
        msg: String,
        t: Throwable?,
    ) {
        plugins.plugins.value
            .filterIsInstance<LogRecordSink>()
            .forEach { it.record(severity, tag, msg, t) }
    }

    internal fun export(): String {
        val sb = StringBuilder()
        plugins.plugins.value.forEach { p ->
            p.export().takeIf { it.isNotBlank() }?.let { sb.append("--- ${p.name} ---\n$it\n\n") }
        }
        return sb.toString().trim()
    }

    internal fun clearAll(): Unit = lifecycle.clearAll()
}
