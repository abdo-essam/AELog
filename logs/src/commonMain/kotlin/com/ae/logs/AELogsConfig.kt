package com.ae.logs

import com.ae.logs.plugins.logs.model.LogSeverity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration for a [AELogs] instance.
 *
 * Contains only core (non-UI) settings. UI-specific config lives in
 * `AELogsUiConfig` in the `logs-ui` module.
 *
 * ```kotlin
 * AELogs.create(AELogsConfig())
 * ```
 */
public data class AELogsConfig(
    val isEnabled: Boolean = true,
    val minSeverity: LogSeverity = LogSeverity.VERBOSE,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val platformLogSink: PlatformLogSink = PlatformLogSink.Default,
    val errorHandler: (Throwable) -> Unit = {
        platformLogSink.log(LogSeverity.ERROR, "AELogs", "Plugin error", it)
    },
)
