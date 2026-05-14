package com.ae.log.plugin

/**
 * Generic filter base for AELog plugins.
 *
 * Each plugin defines its own subtype with a concrete [T], e.g.:
 * - `LogSeverityFilter : PluginFilter<LogEntry>`
 * - `NetworkFilter : PluginFilter<NetworkEntry>`
 * - `AnalyticsFilter : PluginFilter<AnalyticsEvent>`
 */
public open class PluginFilter<T>(
    public val label: String,
    private val predicate: (T) -> Boolean,
) {
    public open fun matches(item: T): Boolean = predicate(item)
}
