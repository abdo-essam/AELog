package com.ae.logs.plugins.analytics.model

import androidx.compose.runtime.Immutable

/** A single analytics event with arbitrary properties. */
@Immutable
public data class AnalyticsEvent(
    /** Unique ID for stable list keys. */
    val id: String,
    /** Event name, e.g. `"button_tap"`, `"screen_view"`. */
    val name: String,
    /** Arbitrary key-value properties. */
    val properties: Map<String, Any> = emptyMap(),
    /** Set by [com.ae.logs.plugins.analytics.AnalyticsApi] at track time (epoch millis). */
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
