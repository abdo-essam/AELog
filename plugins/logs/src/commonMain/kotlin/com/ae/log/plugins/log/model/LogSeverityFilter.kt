package com.ae.log.plugins.log.model

import com.ae.log.plugin.PluginFilter

/**
 * Severity-based filter for the log viewer.
 *
 * Extends [PluginFilter] so the label/matches contract is shared across
 * all plugin filter hierarchies.
 */
public open class LogSeverityFilter(
    label: String,
    predicate: (LogEntry) -> Boolean,
) : PluginFilter<LogEntry>(label, predicate)

public object LogSeverityFilters {
    public val ALL: LogSeverityFilter = LogSeverityFilter("All") { true }
    public val VERBOSE: LogSeverityFilter = LogSeverityFilter("Verbose") { it.severity == LogSeverity.VERBOSE }
    public val DEBUG: LogSeverityFilter = LogSeverityFilter("Debug") { it.severity == LogSeverity.DEBUG }
    public val INFO: LogSeverityFilter = LogSeverityFilter("Info") { it.severity == LogSeverity.INFO }
    public val WARN: LogSeverityFilter = LogSeverityFilter("Warn") { it.severity == LogSeverity.WARN }
    public val ERROR: LogSeverityFilter =
        LogSeverityFilter("Error") {
            it.severity == LogSeverity.ERROR || it.severity == LogSeverity.ASSERT
        }

    public val defaultFilters: List<LogSeverityFilter> = listOf(ALL, VERBOSE, DEBUG, INFO, WARN, ERROR)
}
