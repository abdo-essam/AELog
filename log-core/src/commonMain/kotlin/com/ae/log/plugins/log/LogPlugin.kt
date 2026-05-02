@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.log

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.core.PluginContext
import com.ae.log.core.UIPlugin
import com.ae.log.core.bus.subscribe
import com.ae.log.core.store.PluginStore
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.plugins.log.ui.LogContent
import com.ae.log.plugins.log.ui.LogViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

public typealias LogStore = PluginStore<LogEntry>

/**
 * Built-in logs plugin for AELog.
 *
 * Provides a full-featured log viewer with search, filtering, and copy capabilities.
 *
 * ## Installation
 * ```kotlin
 * AELog.init(LogPlugin())
 * ```
 *
 * ## Logging — zero ceremony (preferred)
 *
 * Use the static shorthands on the [AELog] companion object. No instance,
 * no nullable chain, no plugin lookup required:
 *
 * ```kotlin
 * AELog.v("MyTag", "Verbose detail")
 * AELog.d("MyTag", "Debug info")
 * AELog.i("MyTag", "Something happened")
 * AELog.w("MyTag", "Watch out")
 * AELog.e("MyTag", "Something went wrong", throwable)
 * AELog.wtf("MyTag", "Should never happen", throwable)
 * ```
 *
 * All calls are **silent no-ops** if [AELog.init] has not been called yet —
 * consistent with Timber's behaviour before a Tree is planted.
 *
 * ## Logging — on an instance (advanced / multi-instance)
 *
 * If you hold a specific [AELog] instance (e.g. in tests or an embedded SDK):
 *
 * ```kotlin
 * inspector.log(LogSeverity.INFO, "MyTag", "Something happened")
 * // or directly through the plugin:
 * inspector.getPlugin<LogPlugin>()?.api?.i("MyTag", "Something happened")
 * ```
 */
public class LogPlugin(
    maxEntries: Int = 500,
) : UIPlugin {
    override val id: String = ID
    override val name: String = "Logs"
    override val icon: ImageVector = Icons.Default.Description

    internal val logStore = PluginStore<LogEntry>(capacity = maxEntries)

    /** Public write API — use this to send logs to the viewer. */
    public val api: LogApi = LogApi(logStore)

    private val _badgeCount = MutableStateFlow(0)
    override val badgeCount: StateFlow<Int> = _badgeCount

    private var viewModel: LogViewModel? = null

    /**
     * Starts observing the log store to keep [badgeCount] in sync.
     * Creates [LogViewModel] bound to the plugin's managed scope.
     */
    override fun onAttach(context: PluginContext) {
        viewModel = LogViewModel(logStore = logStore, scope = context.scope)

        context.scope.launch {
            logStore.dataFlow.collect { logs ->
                _badgeCount.value = logs.size
            }
        }

        context.scope.launch {
            context.eventBus
                .subscribe<com.ae.log.core.bus.RegisterLogTagEvent>()
                .collect { event ->
                    com.ae.log.plugins.log.model.LogTagRegistry
                        .register(event.tag, event.badgeLabel)
                }
        }
    }

    override fun onClear() {
        logStore.clear()
    }

    override fun onDetach() {
        // context.scope is cancelled by AELog before this call —
        // all coroutines are stopped. Only clean up non-coroutine state here.
        viewModel = null
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        LogContent(
            viewModel = vm,
            modifier = modifier,
        )
    }

    override fun export(): String =
        logStore.dataFlow.value.joinToString("\n") { log ->
            "[${log.severity.name}] ${log.tag}: ${log.message}"
        }

    public companion object {
        public const val ID: String = "ae_logs_logs"
    }
}

/** Type-safe accessor for the [LogApi] on the default [com.ae.log.AELog] instance. */
public val com.ae.log.AELog.Companion.log: LogApi?
    get() = plugin<LogPlugin>()?.api
