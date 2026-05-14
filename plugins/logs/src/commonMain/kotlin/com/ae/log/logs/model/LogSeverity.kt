package com.ae.log.logs.model

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
