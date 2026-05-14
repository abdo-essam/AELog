package com.ae.log.analytics.model

import com.ae.log.plugin.PluginFilter

/**
 * Filter options for the analytics panel.
 *
 * Extends [PluginFilter] so the label/matches contract is shared across
 * all plugin filter hierarchies.
 */
public open class AnalyticsFilter(
    label: String,
    predicate: (AnalyticsEvent) -> Boolean,
) : PluginFilter<AnalyticsEvent>(label, predicate)

public object AnalyticsFilters {
    public val ALL: AnalyticsFilter = AnalyticsFilter("All") { true }
    public val SCREENS: AnalyticsFilter = AnalyticsFilter("Screens") { it.name == "screen_view" }
    public val EVENTS: AnalyticsFilter = AnalyticsFilter("Events") { it.name != "screen_view" }

    public val defaultFilters: List<AnalyticsFilter> = listOf(ALL, SCREENS, EVENTS)
}
