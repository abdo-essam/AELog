package com.ae.logs.plugins.logs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.logs.core.PluginContext
import com.ae.logs.core.UIPlugin
import com.ae.logs.plugins.logs.store.LogStore
import com.ae.logs.plugins.logs.ui.LogsContent
import com.ae.logs.plugins.logs.ui.LogsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

/**
 * Built-in logs plugin for AELogs.
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
 * ## Convenience extension (from `logs` aggregator)
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
                .filterIsInstance<com.ae.logs.core.bus.RegisterLogTagEvent>()
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

    public companion object {
        public const val ID: String = "ae_logs_logs"
    }
}

// ── Convenience extensions ────────────────────────────────────────────────────

/**
 * Log a message to the built-in [LogsPlugin].
 *
 * No-op if [LogsPlugin] is not installed.
 */
public fun com.ae.logs.AELogs.log(
    severity: com.ae.logs.plugins.logs.model.LogSeverity,
    tag: String,
    message: String,
) {
    getPlugin<LogsPlugin>()?.api?.log(severity, tag, message)
}

public fun com.ae.logs.AELogs.Companion.v(
    tag: String,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
    default.log(com.ae.logs.plugins.logs.model.LogSeverity.VERBOSE, tag, fullMessage)
}

public fun com.ae.logs.AELogs.Companion.d(
    tag: String,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
    default.log(com.ae.logs.plugins.logs.model.LogSeverity.DEBUG, tag, fullMessage)
}

public fun com.ae.logs.AELogs.Companion.i(
    tag: String,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
    default.log(com.ae.logs.plugins.logs.model.LogSeverity.INFO, tag, fullMessage)
}

public fun com.ae.logs.AELogs.Companion.w(
    tag: String,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
    default.log(com.ae.logs.plugins.logs.model.LogSeverity.WARN, tag, fullMessage)
}

public fun com.ae.logs.AELogs.Companion.e(
    tag: String,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
    default.log(com.ae.logs.plugins.logs.model.LogSeverity.ERROR, tag, fullMessage)
}

public fun com.ae.logs.AELogs.Companion.wtf(
    tag: String,
    message: String,
    throwable: Throwable? = null,
) {
    val fullMessage = if (throwable != null) "$message\n${throwable.stackTraceToString()}" else message
    default.log(com.ae.logs.plugins.logs.model.LogSeverity.ASSERT, tag, fullMessage)
}
