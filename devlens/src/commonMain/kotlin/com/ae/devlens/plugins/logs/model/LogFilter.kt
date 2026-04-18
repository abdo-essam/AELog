package com.ae.devlens.plugins.logs.model

/**
 * Filter options for the log viewer.
 */
public enum class LogFilter(
    public val label: String,
) {
    ALL("All"),
    NETWORK("Network"),
    ANALYTICS("Analytics"),
}
