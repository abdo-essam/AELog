package com.ae.devlens.plugins.logs.model

/**
 * Severity-based filter for the log viewer.
 *
 * Network and Analytics now have dedicated plugins with their own panels —
 * these filters only cover log severity levels.
 */
public enum class LogFilter(
    public val label: String,
) {
    ALL("All"),
    VERBOSE("Verbose"),
    DEBUG("Debug"),
    INFO("Info"),
    WARN("Warn"),
    ERROR("Error"),
}
