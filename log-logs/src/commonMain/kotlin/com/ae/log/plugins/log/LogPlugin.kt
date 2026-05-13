package com.ae.log.plugins.log

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.core.storage.PluginStorage
import com.ae.log.plugin.PluginContext
import com.ae.log.plugin.UIPlugin
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.plugins.log.model.LogSeverity
import com.ae.log.plugins.log.ui.LogContent
import com.ae.log.plugins.log.ui.LogViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

public typealias LogStorage = PluginStorage<LogEntry>

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
 * ## Logging
 * ```kotlin
 * AELog.log.v("MyTag", "Verbose detail")
 * AELog.log.d("MyTag", "Debug info")
 * AELog.log.i("MyTag", "Something happened")
 * AELog.log.w("MyTag", "Watch out")
 * AELog.log.e("MyTag", "Something went wrong", throwable)
 * AELog.log.wtf("MyTag", "Should never happen", throwable)
 * ```
 *
 * All calls are **silent no-ops** if [AELog.init] has not been called yet.
 */
public class LogPlugin(
    public val maxEntries: Int = 500,
    public val minSeverity: LogSeverity = LogSeverity.VERBOSE,
    public val platformLogSink: PlatformLogSink = PlatformLogSink.Default,
) : UIPlugin,
    LogRecordSink {
    override val id: String = ID
    override val name: String = "Logs"
    override val icon: ImageVector = Icons.Default.Description

    internal val logStorage = PluginStorage<LogEntry>(capacity = maxEntries)

    /** Public write API — use this to send logs directly to the viewer. */
    public val recorder: LogRecorder =
        LogRecorder(
            storage = logStorage,
            minSeverity = minSeverity,
            platformLogSink = platformLogSink,
        )

    private val _badgeCount = MutableStateFlow(0)
    override val badgeCount: StateFlow<Int> = _badgeCount

    @kotlin.concurrent.Volatile private var viewModel: LogViewModel? = null

    override fun onAttach(context: PluginContext) {
        viewModel = LogViewModel(logStorage = logStorage, scope = context.scope)

        context.scope.launch {
            logStorage.dataFlow.collect { logs ->
                _badgeCount.value = logs.size
            }
        }
    }

    /** Routes [AELog.record] calls to this plugin's recorder via the [LogRecordSink] interface. */
    override fun record(
        severity: LogSeverity,
        tag: String,
        msg: String,
        throwable: Throwable?,
    ) {
        recorder.log(severity, tag, msg, throwable)
    }

    override fun onClear() {
        logStorage.clear()
    }

    override fun onDetach() {
        viewModel = null
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        LogContent(viewModel = vm, modifier = modifier)
    }

    override fun export(): String =
        logStorage.dataFlow.value.joinToString("\n") { log ->
            "[${log.severity.label}] ${log.tag}: ${log.message}"
        }

    public companion object {
        public const val ID: String = "ae_logs_logs"
    }
}
