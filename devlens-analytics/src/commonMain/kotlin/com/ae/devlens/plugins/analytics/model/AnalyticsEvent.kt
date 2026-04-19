package com.ae.devlens.plugins.analytics.model

/** A single analytics event with arbitrary string properties. */
public data class AnalyticsEvent(
    /** Unique ID for stable list keys. */
    val id: String,
    /** Event name, e.g. `"button_tap"`, `"screen_view"`. */
    val name: String,
    /** Arbitrary key-value properties, e.g. `mapOf("screen" to "home")`. */
    val properties: Map<String, String> = emptyMap(),
    /** Set by [com.ae.devlens.plugins.analytics.AnalyticsApi] at track time (epoch millis). */
    val timestamp: Long,
    /** Optional source adapter name (e.g. `"Firebase"`, `"Mixpanel"`). */
    val source: String? = null,
)
