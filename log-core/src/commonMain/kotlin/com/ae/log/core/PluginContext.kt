package com.ae.log.core

import com.ae.log.LogConfig
import com.ae.log.core.bus.Event
import com.ae.log.core.bus.EventBus
import com.ae.log.core.bus.subscribe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

public interface PluginContext {
    public val scope: CoroutineScope
    public val config: LogConfig
    public val eventBus: EventBus

    public fun <T : Plugin> getPlugin(type: KClass<T>): T?
}

public inline fun <reified T : Plugin> PluginContext.getPlugin(): T? = getPlugin(T::class)

public inline fun <reified T : Event> PluginContext.collectEvents(crossinline action: suspend (T) -> Unit): Job =
    scope.launch { eventBus.subscribe<T>().collect { action(it) } }
