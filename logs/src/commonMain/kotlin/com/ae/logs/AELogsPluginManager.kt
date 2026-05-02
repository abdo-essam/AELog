package com.ae.logs

import com.ae.logs.core.AELogsPlugin
import com.ae.logs.core.PluginContext
import com.ae.logs.core.bus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlin.reflect.KClass
import kotlinx.atomicfu.getAndUpdate as atomicGetAndUpdate
import kotlinx.atomicfu.update as atomicUpdate

/**
 * Manages the full lifecycle of registered [AELogsPlugin]s.
 *
 * Owns per-plugin [CoroutineScope]s (via [SupervisorJob]) so they are
 * automatically cancelled when the plugin is detached — callers never
 * need to manage scopes manually.
 *
 * This class is internal to `logs-core`. Consumers interact with plugins
 * through [AELogs], which delegates here.
 *
 * ## Responsibilities
 * - Install / uninstall plugins (de-duplicated by ID)
 * - Build and pass [PluginContext] on attach
 * - Cancel plugin scope before calling [AELogsPlugin.onDetach]
 * - Provide type-safe and ID-based plugin lookup
 */
public class AELogsPluginManager internal constructor(
    private val config: AELogsConfig,
    private val eventBus: EventBus,
) {
    private val _plugins = MutableStateFlow<List<AELogsPlugin>>(emptyList())

    /** Hot stream of all currently registered plugins. */
    val plugins: StateFlow<List<AELogsPlugin>> = _plugins.asStateFlow()

    /** Per-plugin coroutine scopes, keyed by plugin id. */
    private val scopes = kotlinx.atomicfu.atomic(emptyMap<String, CoroutineScope>())

    // ── Registration ──────────────────────────────────────────────────────────

    public fun install(plugin: AELogsPlugin): AELogsPluginManager {
        val wasAdded =
            _plugins
                .updateAndGet { current ->
                    if (current.any { it.id == plugin.id }) current else current + plugin
                }.contains(plugin)

        if (wasAdded && !scopes.value.containsKey(plugin.id)) {
            val scope = CoroutineScope(SupervisorJob() + config.dispatcher)
            scopes.atomicUpdate { it + (plugin.id to scope) }
            safeCall(plugin.id) { plugin.onAttach(buildContext(scope)) }
        }
        return this
    }

    public fun uninstall(pluginId: String): AELogsPluginManager {
        var detached: AELogsPlugin? = null
        _plugins.update { current ->
            val plugin = current.find { it.id == pluginId } ?: return@update current
            detached = plugin
            current.filter { it.id != pluginId }
        }
        detached?.let { plugin ->
            val removedScope = scopes.atomicGetAndUpdate { it - plugin.id }[plugin.id]
            removedScope?.cancel()
            safeCall(plugin.id) { plugin.onDetach() }
        }
        return this
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    public fun <T : AELogsPlugin> getPlugin(type: KClass<T>): T? =
        _plugins.value.firstOrNull { type.isInstance(it) } as? T

    public fun getPluginById(id: String): AELogsPlugin? = _plugins.value.find { it.id == id }

    // ── Batch iteration ───────────────────────────────────────────────────────

    /** Iterate all plugins safely — one failure doesn't stop the rest. */
    internal fun forEach(action: (AELogsPlugin) -> Unit) = _plugins.value.forEach { safeCall(it.id) { action(it) } }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun buildContext(scope: CoroutineScope): PluginContext =
        object : PluginContext {
            override val scope: CoroutineScope = scope
            override val config: AELogsConfig = this@AELogsPluginManager.config
            override val eventBus: EventBus = this@AELogsPluginManager.eventBus

            @Suppress("UNCHECKED_CAST")
            override fun <T : AELogsPlugin> getPlugin(type: KClass<T>): T? = this@AELogsPluginManager.getPlugin(type)
        }

    companion object {
        /** Runs [block] and swallows exceptions so one bad plugin can't crash others. */
        fun safeCall(
            pluginId: String,
            block: () -> Unit,
        ) {
            runCatching { block() }
                .onFailure { error ->
                    AELogs
                        .defaultOrNull()
                        ?.config
                        ?.errorHandler
                        ?.invoke(error)
                }
        }
    }
}
