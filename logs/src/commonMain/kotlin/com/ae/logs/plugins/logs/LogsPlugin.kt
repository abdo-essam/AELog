package com.ae.logs.plugins.logs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.logs.core.PluginContext
import com.ae.logs.core.UIPlugin
import com.ae.logs.core.bus.subscribe
import com.ae.logs.plugins.logs.store.LogStore
import com.ae.logs.plugins.logs.ui.LogsContent
import com.ae.logs.plugins.logs.ui.LogsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Built-in logs plugin for AELogs.
 *
 * Provides a full-featured log viewer with search, filtering, and copy capabilities.
 *
 * ## Installation
 * ```kotlin
 * AELogs.init(LogsPlugin())
 * ```
 *
 * ## Logging — zero ceremony (preferred)
 *
 * Use the static shorthands on the [AELogs] companion object. No instance,
 * no nullable chain, no plugin lookup required:
 *
 * ```kotlin
 * AELogs.v("MyTag", "Verbose detail")
 * AELogs.d("MyTag", "Debug info")
 * AELogs.i("MyTag", "Something happened")
 * AELogs.w("MyTag", "Watch out")
 * AELogs.e("MyTag", "Something went wrong", throwable)
 * AELogs.wtf("MyTag", "Should never happen", throwable)
 * ```
 *
 * All calls are **silent no-ops** if [AELogs.init] has not been called yet —
 * consistent with Timber's behaviour before a Tree is planted.
 *
 * ## Logging — on an instance (advanced / multi-instance)
 *
 * If you hold a specific [AELogs] instance (e.g. in tests or an embedded SDK):
 *
 * ```kotlin
 * inspector.log(LogSeverity.INFO, "MyTag", "Something happened")
 * // or directly through the plugin:
 * inspector.getPlugin<LogsPlugin>()?.api?.i("MyTag", "Something happened")
 * ```
 */
public class LogsPlugin(
    maxEntries: Int = 500,
) : UIPlugin {
    override val id: String = ID
    override val name: String = "Logs"
    override val icon: ImageVector = Icons.Default.Description

    internal val logStore = LogStore(maxEntries = maxEntries)

    /** Public write API — use this to send logs to the viewer. */
    public val api: LogsApi = LogsApi(logStore)

    private val _badgeCount = MutableStateFlow(0)
    override val badgeCount: StateFlow<Int> = _badgeCount

    private var viewModel: LogsViewModel? = null

    /**
     * Starts observing the log store to keep [badgeCount] in sync.
     * Creates [LogsViewModel] bound to the plugin's managed scope.
     */
    override fun onAttach(context: PluginContext) {
        viewModel = LogsViewModel(logStore = logStore, scope = context.scope)

        context.scope.launch {
            logStore.logsFlow.collect { logs ->
                _badgeCount.value = logs.size
            }
        }

        context.scope.launch {
            context.eventBus
                .subscribe<com.ae.logs.core.bus.RegisterLogTagEvent>()
                .collect { event ->
                    com.ae.logs.plugins.logs.model.LogTagRegistry
                        .register(event.tag, event.badgeLabel)
                }
        }
    }

    override fun onClear() {
        logStore.clear()
    }

    override fun onDetach() {
        // context.scope is cancelled by AELogs before this call —
        // all coroutines are stopped. Only clean up non-coroutine state here.
        viewModel = null
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        LogsContent(
            viewModel = vm,
            modifier = modifier,
        )
    }

    override fun export(): String =
        logStore.logsFlow.value.joinToString("\n") { log ->
            "[${log.severity.name}] ${log.tag}: ${log.message}"
        }

    public companion object {
        public const val ID: String = "ae_logs_logs"
    }
}

/** Type-safe accessor for the [LogsApi] on the default [com.ae.logs.AELogs] instance. */
public val com.ae.logs.AELogs.Companion.logs: LogsApi?
    get() = plugin<LogsPlugin>()?.api
