package com.ae.log.plugin

import kotlinx.coroutines.CoroutineScope
import kotlin.reflect.KClass

public interface PluginContext {
    public val scope: CoroutineScope

    public fun <T : Plugin> getPlugin(type: KClass<T>): T?
}


public inline fun <reified T : Plugin> PluginContext.getPlugin(): T? = getPlugin(T::class)
