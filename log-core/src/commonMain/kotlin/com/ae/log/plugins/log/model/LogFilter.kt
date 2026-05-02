package com.ae.log.plugins.log.model

/**
 * Severity-based filter for the log viewer.
 *
 * Network and Analytics now have dedicated plugins with their own panels —
 * these filters only cover log severity levels.
 */
public open class LogFilter(
    public val label: String,
    private val predicate: (LogEntry) -> Boolean,
) {
    public open fun matches(entry: LogEntry): Boolean = predicate(entry)
}

public object LogFilters {
    public val ALL: LogFilter = LogFilter("All") { true }
    public val VERBOSE: LogFilter = LogFilter("Verbose") { it.severity == LogSeverity.VERBOSE }
    public val DEBUG: LogFilter = LogFilter("Debug") { it.severity == LogSeverity.DEBUG }
    public val INFO: LogFilter = LogFilter("Info") { it.severity == LogSeverity.INFO }
    public val WARN: LogFilter = LogFilter("Warn") { it.severity == LogSeverity.WARN }
    public val ERROR: LogFilter =
        LogFilter("Error") {
            it.severity == LogSeverity.ERROR ||
                it.severity == LogSeverity.ASSERT
        }

    public val defaultFilters: List<LogFilter> = listOf(ALL, VERBOSE, DEBUG, INFO, WARN, ERROR)
}
