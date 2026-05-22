package com.ae.log.crashes

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.crashes.capture.CrashRecorder
import com.ae.log.crashes.capture.PlatformCrashHandler
import com.ae.log.crashes.storage.CrashStorage
import com.ae.log.crashes.ui.CrashContent
import com.ae.log.crashes.ui.CrashViewModel
import com.ae.log.plugin.PluginContext
import com.ae.log.plugin.UIPlugin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Built-in crash reporting plugin for AELog.
 *
 * Captures fatal and non-fatal exceptions across all platforms, persists them
 * to disk so they survive app restarts, and presents them in a searchable,
 * filterable panel inside the AELog overlay.
 *
 * ## Zero-config installation — all platforms
 * ```kotlin
 * AELog.init(CrashPlugin())
 * ```
 * On Android, a self-initializing [ContentProvider][android.content.ContentProvider] captures
 * the application context automatically (same technique as WorkManager and Firebase).
 * No explicit Context or path setup is required.
 *
 * ## Reporting non-fatal exceptions
 * ```kotlin
 * try {
 *     riskyOperation()
 * } catch (e: Exception) {
 *     AELog.crashes.recordNonFatal(e)
 * }
 * ```
 *
 * Fatal crashes are captured automatically via a platform uncaught-exception hook.
 * The previous handler is always chained — the system crash dialog still appears.
 *
 * @param storageDir Absolute path to the directory where crash events are persisted.
 *   Defaults to a platform-appropriate private directory.
 *   On Android prefer [CrashPlugin(context)] to get the proper files directory.
 */
public class CrashPlugin(
    private val storageDir: String = defaultCrashStorageDir(),
) : UIPlugin {
    override val id: String = ID
    override val name: String = "Crashes"
    override val icon: ImageVector = Icons.Default.BugReport

    internal val storage = CrashStorage(directoryPath = storageDir)
    internal val recorder = CrashRecorder(storage = storage)
    private val handler = PlatformCrashHandler(recorder = recorder)

    private val _badgeCount = MutableStateFlow(0)
    override val badgeCount: StateFlow<Int> = _badgeCount

    @kotlin.concurrent.Volatile private var viewModel: CrashViewModel? = null

    override fun onAttach(context: PluginContext) {
        // Install the crash handler immediately so fatals are captured
        // from the moment AELog.init() is called — before any UI is shown.
        handler.install()
        viewModel = CrashViewModel(storage = storage, scope = context.scope)
        context.scope.launch {
            storage.events.collect { events ->
                _badgeCount.value = events.size
            }
        }
    }

    // onStart/onStop are tied to the UI overlay lifecycle in this framework,
    // not to the app lifecycle. The crash handler must be independent of the UI.
    override fun onStart(): Unit = Unit

    override fun onStop(): Unit = Unit

    override fun onClear() {
        storage.clear()
    }

    override fun onDetach() {
        handler.uninstall()
        viewModel = null
    }

    /**
     * Manually records a non-fatal exception.
     *
     * Call this anywhere you catch a recoverable error that you still want
     * to track in the crash viewer.
     */
    public fun recordNonFatal(
        throwable: Throwable,
        threadName: String = "main",
    ) {
        recorder.record(
            throwable = throwable,
            threadName = threadName,
            isFatal = false,
        )
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        CrashContent(viewModel = vm, modifier = modifier)
    }

    override fun export(): String =
        storage.events.value.joinToString("\n${"-".repeat(60)}\n") { event ->
            "[${if (event.isFatal) "FATAL" else "NON-FATAL"}] ${event.exceptionType}: ${event.message}"
        }

    public companion object {
        public const val ID: String = "ae_crashes"
    }
}
