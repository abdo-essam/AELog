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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Built-in logs plugin for DevLens.
 *
 * Provides a full-featured log viewer with search, filtering, and copy capabilities.
 * This plugin is installed by default when creating an [com.ae.devlens.AEDevLens] instance.
 *
 * ```kotlin
 * val logsPlugin = inspector.getPlugin<LogsPlugin>()
 * logsPlugin?.logStore?.log(LogSeverity.INFO, "MyTag", "Hello!")
 * ```
 */
public class LogsPlugin(
    internal val logStore: LogStore = LogStore(),
) : UIPlugin {

    override val id: String = ID
    override val name: String = "Logs"
    override val icon: ImageVector = Icons.Default.Description

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    private var onCloseCallback: (() -> Unit)? = null

    /**
     * Starts observing [LogStore] to keep [badgeCount] in sync.
     *
     * Coroutines are launched on [context.scope] — no manual cancellation needed;
     * the scope is cancelled automatically when the plugin is detached.
     */
    override fun onAttach(context: PluginContext) {
        context.scope.launch {
            logStore.logsFlow.collect { logs ->
                _badgeCount.value = if (logs.isEmpty()) null else logs.size
            }
        }
    }

    override fun onOpen() {
        // Could resume expensive computed values here
    }

    override fun onClose() {
        // Could pause expensive operations here
    }

    override fun onClear() {
        logStore.clear()
    }

    override fun onDetach() {
        // context.scope is already cancelled by AEDevLens before this is called —
        // all collector coroutines are stopped. Only clean up non-coroutine resources here.
        logStore.destroy()
        onCloseCallback = null
    }

    internal fun setOnCloseCallback(callback: () -> Unit) {
        onCloseCallback = callback
    }

    @Composable
    override fun Content(modifier: Modifier) {
        LogsContent(
            logStore = logStore,
            modifier = modifier,
            onCloseInspector = { onCloseCallback?.invoke() },
        )
    }

    public companion object {
        public const val ID: String = "ae_devlens_logs"
    }
}
