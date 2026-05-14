package com.ae.log.plugins.analytics.model

import androidx.compose.runtime.Stable

/** A single analytics event with arbitrary properties. */
@Stable
public data class AnalyticsEvent(
    /** Unique ID for stable list keys. */
    val id: String,
    /** Event name, e.g. `"button_tap"`, `"screen_view"`. */
    val name: String,
    /** Key-value properties. Values are stringified for safe immutability. */
    val properties: Map<String, String> = emptyMap(),
    /** Set by [com.ae.log.plugins.analytics.AnalyticsTracker] at track time (epoch millis). */
    val timestamp: Long,
    /** Optional source adapter name to prevent drift (e.g. `"Firebase"`, `"Mixpanel"`). */
    val source: AdapterSource? = null,
)

/** Source of the analytics event to prevent string-drift across plugins. */
public interface AdapterSource {
    public val sourceName: String
}

public enum class DefaultAdapterSource(
    override val sourceName: String,
) : AdapterSource {
    FIREBASE("Firebase"),
    MIXPANEL("Mixpanel"),
    AMPLITUDE("Amplitude"),
    CUSTOM("Custom"),
}
