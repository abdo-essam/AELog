package com.ae.log.plugins.analytics.model

/** Filter options for the analytics panel. */
public open class AnalyticsFilter(
    public val label: String,
    private val predicate: (AnalyticsEvent) -> Boolean,
) {
    public open fun matches(event: AnalyticsEvent): Boolean = predicate(event)
}

public object AnalyticsFilters {
    public val ALL: AnalyticsFilter = AnalyticsFilter("All") { true }
    public val SCREENS: AnalyticsFilter = AnalyticsFilter("Screens") { it.name == "screen_view" }
    public val EVENTS: AnalyticsFilter = AnalyticsFilter("Events") { it.name != "screen_view" }

    public val defaultFilters: List<AnalyticsFilter> = listOf(ALL, SCREENS, EVENTS)
}
