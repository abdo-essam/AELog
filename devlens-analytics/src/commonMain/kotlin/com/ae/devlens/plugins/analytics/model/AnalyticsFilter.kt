package com.ae.devlens.plugins.analytics.model

/** Filter options for the analytics panel. */
public enum class AnalyticsFilter(
    public val label: String,
) {
    ALL("All"),
    SCREENS("Screens"),
    EVENTS("Events"),
}
