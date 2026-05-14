package com.ae.log.plugin

import com.ae.log.config.LogConfig
import com.ae.log.event.EventBus
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

public class PluginManager internal constructor(
    private val config: LogConfig,
    private val eventBus: EventBus,
) {
    private val _plugins = MutableStateFlow<List<Plugin>>(emptyList())
    public val plugins: StateFlow<List<Plugin>> = _plugins.asStateFlow()

    private val scopes = mutableMapOf<String, CoroutineScope>()
    private val installLock = SynchronizedObject()

    public fun install(plugin: Plugin): PluginManager {
        synchronized(installLock) {
            if (_plugins.value.any { it.id == plugin.id }) return@synchronized
            _plugins.update { it + plugin }
            val scope = CoroutineScope(SupervisorJob() + config.dispatcher)
            scopes[plugin.id] = scope
            safeCall { plugin.onAttach(buildContext(scope)) }
        }
        return this
    }

    public fun uninstall(pluginId: String): PluginManager {
        synchronized(installLock) {
            val plugin = _plugins.value.find { it.id == pluginId } ?: return@synchronized
            _plugins.update { current -> current.filter { it.id != pluginId } }
            scopes.remove(pluginId)?.cancel()
            safeCall { plugin.onDetach() }
        }
        return this
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T : Plugin> getPlugin(type: KClass<T>): T? = _plugins.value.firstOrNull { type.isInstance(it) } as? T

    public fun getPluginById(id: String): Plugin? = _plugins.value.find { it.id == id }

    internal fun forEach(action: (Plugin) -> Unit) = _plugins.value.forEach { safeCall { action(it) } }

    private fun buildContext(scope: CoroutineScope): PluginContext =
        object : PluginContext {
            override val scope = scope
            override val config = this@PluginManager.config
            override val eventBus = this@PluginManager.eventBus

            @Suppress("UNCHECKED_CAST")
            override fun <T : Plugin> getPlugin(type: KClass<T>): T? = this@PluginManager.getPlugin(type)
        }

    private fun safeCall(block: () -> Unit) {
        runCatching(block).onFailure { config.errorHandler.invoke(it) }
    }
}
