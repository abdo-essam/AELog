package com.ae.log

import com.ae.log.plugins.log.model.LogSeverity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

public data class LogConfig(
    val enabled: Boolean = true,
    val platformLogSink: PlatformLogSink = PlatformLogSink.Default,
    val minSeverity: LogSeverity = LogSeverity.VERBOSE,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val errorHandler: (Throwable) -> Unit = {},
)
