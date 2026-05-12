package com.ae.log

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Global configuration for the AELog SDK.
 */
public data class LogConfig(
    val enabled: Boolean = true,
    val dispatcher: CoroutineDispatcher = Dispatchers.Default,
    val errorHandler: (Throwable) -> Unit = {},
)
