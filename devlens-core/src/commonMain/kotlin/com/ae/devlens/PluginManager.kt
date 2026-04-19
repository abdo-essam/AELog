package com.ae.devlens

import com.ae.devlens.core.DevLensPlugin
import com.ae.devlens.core.PluginContext
import com.ae.devlens.core.bus.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.reflect.KClass

/**
 * Manages the full lifecycle of registered [DevLensPlugin]s.
 *
 * Owns per-plugin [CoroutineScope]s (via [SupervisorJob]) so they are
 * automatically cancelled when the plugin is detached — callers never
 * need to manage scopes manually.
 *
 * This class is internal to `devlens-core`. Consumers interact with plugins
 * through [AEDevLens], which delegates here.
 *
 * ## Responsibilities
 * - Install / uninstall plugins (de-duplicated by ID)
 * - Build and pass [PluginContext] on attach
 * - Cancel plugin scope before calling [DevLensPlugin.onDetach]
 * - Provide type-safe and ID-based plugin lookup
 */
internal class PluginManager(
    private val config: DevLensConfig,
    private val eventBus: EventBus,
) {
    private val _plugins = MutableStateFlow<List<DevLensPlugin>>(emptyList())

    /** Hot stream of all currently registered plugins. */
    val plugins: StateFlow<List<DevLensPlugin>> = _plugins.asStateFlow()

    /** Per-plugin coroutine scopes, keyed by plugin id. */
    private val scopes = mutableMapOf<String, CoroutineScope>()

    // ── Registration ──────────────────────────────────────────────────────────

    fun install(plugin: DevLensPlugin) {
        var attached = false
        _plugins.update { current ->
            if (current.any { it.id == plugin.id }) {
                current
            } else {
                attached = true
                current + plugin
            }
        }
        if (attached) {
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
            scopes[plugin.id] = scope
            safeCall(plugin.id) { plugin.onAttach(buildContext(scope)) }
        }
    }

    fun uninstall(pluginId: String) {
        var detached: DevLensPlugin? = null
        _plugins.update { current ->
            val plugin = current.find { it.id == pluginId } ?: return@update current
            detached = plugin
            current.filter { it.id != pluginId }
        }
        detached?.let { plugin ->
            scopes.remove(plugin.id)?.cancel()
            safeCall(plugin.id) { plugin.onDetach() }
        }
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    @Suppress("UNCHECKED_CAST")
    fun <T : DevLensPlugin> getPlugin(type: KClass<T>): T? = _plugins.value.firstOrNull { type.isInstance(it) } as? T

    fun getPluginById(id: String): DevLensPlugin? = _plugins.value.find { it.id == id }

    // ── Batch iteration ───────────────────────────────────────────────────────

    /** Iterate all plugins safely — one failure doesn't stop the rest. */
    fun forEach(action: (DevLensPlugin) -> Unit) = _plugins.value.forEach { safeCall(it.id) { action(it) } }

    // ── Internal ──────────────────────────────────────────────────────────────

    private fun buildContext(scope: CoroutineScope): PluginContext =
        object : PluginContext {
            override val scope: CoroutineScope = scope
            override val config: DevLensConfig = this@PluginManager.config
            override val eventBus: EventBus = this@PluginManager.eventBus

            @Suppress("UNCHECKED_CAST")
            override fun <T : DevLensPlugin> getPlugin(type: KClass<T>): T? = this@PluginManager.getPlugin(type)
        }

    companion object {
        /** Runs [block] and swallows exceptions so one bad plugin can't crash others. */
        fun safeCall(
            pluginId: String,
            block: () -> Unit,
        ) {
            runCatching { block() }
                .onFailure { /* TODO: route to configurable error handler */ }
        }
    }
}
