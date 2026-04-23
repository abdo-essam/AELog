package com.ae.devlens.plugins.logs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.devlens.core.PluginContext
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.plugins.logs.store.LogStore
import com.ae.devlens.plugins.logs.ui.LogsContent
import com.ae.devlens.plugins.logs.ui.LogsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Built-in logs plugin for DevLens.
 *
 * Provides a full-featured log viewer with search, filtering, and copy capabilities.
 *
 * ## Installation
 * ```kotlin
 * inspector.install(LogsPlugin())
 * ```
 *
 * ## Logging via API
 * ```kotlin
 * val logs = inspector.getPlugin<LogsPlugin>()?.api
 * logs?.i("MyTag", "Something happened")
 * ```
 *
 * ## Convenience extension (from `devlens` aggregator)
 * ```kotlin
 * inspector.log(LogSeverity.INFO, "MyTag", "Something happened")
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

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    private var viewModel: LogsViewModel? = null
    private var onCloseCallback: (() -> Unit)? = null

    /**
     * Starts observing the log store to keep [badgeCount] in sync.
     * Creates [LogsViewModel] bound to the plugin's managed scope.
     */
    override fun onAttach(context: PluginContext) {
        viewModel = LogsViewModel(logStore = logStore, scope = context.scope)

        context.scope.launch {
            logStore.logsFlow.collect { logs ->
                _badgeCount.value = if (logs.isEmpty()) null else logs.size
            }
        }

        context.scope.launch {
            context.eventBus.events
                .filterIsInstance<com.ae.devlens.core.bus.RegisterLogTagEvent>()
                .collect { event ->
                    com.ae.devlens.plugins.logs.model.LogTagRegistry.register(event.tag, event.badgeLabel)
                }
        }
    }

    override fun onClear() {
        logStore.clear()
    }

    override fun onDetach() {
        // context.scope is cancelled by AEDevLens before this call —
        // all coroutines are stopped. Only clean up non-coroutine state here.
        viewModel = null
        onCloseCallback = null
    }

    internal fun setOnCloseCallback(callback: () -> Unit) {
        onCloseCallback = callback
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        LogsContent(
            viewModel = vm,
            modifier = modifier,
            onCloseInspector = { onCloseCallback?.invoke() },
        )
    }

    public companion object {
        public const val ID: String = "ae_devlens_logs"
    }
}

// ── Convenience extensions ────────────────────────────────────────────────────

/**
 * Log a message to the built-in [LogsPlugin].
 *
 * No-op if [LogsPlugin] is not installed.
 */
public fun com.ae.devlens.AEDevLens.log(
    severity: com.ae.devlens.plugins.logs.model.LogSeverity,
    tag: String,
    message: String,
) {
    getPlugin<LogsPlugin>()?.api?.log(severity, tag, message)
}
