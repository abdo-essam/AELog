package com.ae.log.plugin

import com.ae.log.config.LogConfig
import com.ae.log.core.event.Event
import com.ae.log.core.event.EventBus
import com.ae.log.core.event.subscribe
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
