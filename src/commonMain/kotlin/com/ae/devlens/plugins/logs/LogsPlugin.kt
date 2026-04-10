package com.ae.devlens.plugins.logs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.devlens.AEDevLens
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.plugins.logs.store.LogStore
import com.ae.devlens.plugins.logs.ui.LogsContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Built-in logs plugin for AEDevLens.
 *
 * Provides a full-featured log viewer with search, filtering, and copy capabilities.
 * This plugin is installed by default when creating an [AEDevLens] instance.
 *
 * ```kotlin
 * val logsPlugin = inspector.getPlugin<LogsPlugin>()
 * logsPlugin?.logStore?.log(LogLevel.INFO, "MyTag", "Hello!")
 * ```
 */
class LogsPlugin(
    internal val logStore: LogStore = LogStore()
) : UIPlugin {

    override val id: String = ID
    override val name: String = "Logs"
    override val icon: ImageVector = Icons.Default.Description

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    private val scope = CoroutineScope(Dispatchers.Default)
    private var badgeJob: Job? = null
    private var onCloseCallback: (() -> Unit)? = null

    override fun onAttach(inspector: AEDevLens) {
        badgeJob = scope.launch {
            logStore.logsFlow.collect { logs ->
                _badgeCount.value = if (logs.isEmpty()) null else logs.size
            }
        }
    }

    override fun onOpen() {
        // Could start refreshing expensive computed values
    }

    override fun onClose() {
        // Could pause expensive operations
    }

    override fun onClear() {
        logStore.clear()
    }

    override fun onDetach() {
        badgeJob?.cancel()
        badgeJob = null
    }

    internal fun setOnCloseCallback(callback: () -> Unit) {
        onCloseCallback = callback
    }

    @Composable
    override fun Content(modifier: Modifier) {
        LogsContent(
            logStore = logStore,
            modifier = modifier,
            onCloseInspector = { onCloseCallback?.invoke() }
        )
    }

    companion object {
        const val ID = "ae_devlens_logs"
    }
}
