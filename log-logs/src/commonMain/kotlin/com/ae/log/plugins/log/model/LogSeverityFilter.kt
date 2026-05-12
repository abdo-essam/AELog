package com.ae.log.plugins.log.model

import com.ae.log.plugins.log.model.LogSeverity

/**
 * Severity-based filter for the log viewer.
 *
 * Network and Analytics now have dedicated plugins with their own panels —
 * these filters only cover log severity levels.
 */
public open class LogSeverityFilter(
    public val label: String,
    private val predicate: (LogEntry) -> Boolean,
) {
    public open fun matches(entry: LogEntry): Boolean = predicate(entry)
}

public object LogSeverityFilters {
    public val ALL: LogSeverityFilter = LogSeverityFilter("All") { true }
    public val VERBOSE: LogSeverityFilter = LogSeverityFilter("Verbose") { it.severity == LogSeverity.VERBOSE }
    public val DEBUG: LogSeverityFilter = LogSeverityFilter("Debug") { it.severity == LogSeverity.DEBUG }
    public val INFO: LogSeverityFilter = LogSeverityFilter("Info") { it.severity == LogSeverity.INFO }
    public val WARN: LogSeverityFilter = LogSeverityFilter("Warn") { it.severity == LogSeverity.WARN }
    public val ERROR: LogSeverityFilter =
        LogSeverityFilter("Error") {
            it.severity == LogSeverity.ERROR ||
                it.severity == LogSeverity.ASSERT
        }

    public val defaultFilters: List<LogSeverityFilter> = listOf(ALL, VERBOSE, DEBUG, INFO, WARN, ERROR)
}
