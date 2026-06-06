package com.ae.log.config

/**
 * Global configuration for the AELog SDK.
 */
public data class LogConfig(
    val enabled: Boolean = true,
    /** Invoked whenever an internal SDK error occurs. Defaults to stderr output. */
    val errorHandler: (Throwable) -> Unit = { t -> println("[AELog] Internal error: ${t.message}") },
    val showNotch: Boolean = true,
)
