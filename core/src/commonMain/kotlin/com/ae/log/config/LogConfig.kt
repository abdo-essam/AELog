package com.ae.log.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Global configuration for the AELog SDK.
 */
public data class LogConfig(
    val enabled: Boolean = true,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    /** Invoked whenever an internal SDK error occurs. Defaults to stderr output. */
    val errorHandler: (Throwable) -> Unit = { t -> println("[AELog] Internal error: ${t.message}") },
)
