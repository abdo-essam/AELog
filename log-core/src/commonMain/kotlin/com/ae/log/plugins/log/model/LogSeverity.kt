package com.ae.log.plugins.log.model

/**
 * Log severity levels.
 *
 * AELog works with any logging library by forwarding logs to this level.
 */
public enum class LogSeverity {
    VERBOSE,
    DEBUG,
    INFO,
    WARN,
    ERROR,
    ASSERT,
    ;

    public val label: String
        get() =
            when (this) {
                VERBOSE -> "V"
                DEBUG -> "D"
                INFO -> "I"
                WARN -> "W"
                ERROR -> "E"
                ASSERT -> "A"
            }
}
