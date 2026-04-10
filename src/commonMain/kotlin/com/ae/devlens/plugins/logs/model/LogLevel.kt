package com.ae.devlens.plugins.logs.model

/**
 * Log severity level.
 *
 * Library-owned enum that replaces Kermit's `Severity` to avoid hard coupling.
 * The [com.ae.devlens.bridge.AEDevLensLogWriter] bridges between Kermit Severity and this.
 */
enum class LogLevel {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ASSERT;

    val label: String
        get() = when (this) {
            VERBOSE -> "V"
            DEBUG -> "D"
            INFO -> "I"
            WARN -> "W"
            ERROR -> "E"
            ASSERT -> "A"
        }
}
