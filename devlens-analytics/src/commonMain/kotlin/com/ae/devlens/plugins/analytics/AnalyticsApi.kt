package com.ae.devlens.plugins.analytics

import com.ae.devlens.plugins.analytics.model.AnalyticsEvent
import com.ae.devlens.plugins.analytics.store.AnalyticsStore
import kotlin.time.Clock

/**
 * Public write-only API for [AnalyticsPlugin].
 *
 * ```kotlin
 * val analytics = AEDevLens.default.getPlugin<AnalyticsPlugin>()?.api
 * analytics?.track("button_tap", mapOf("screen" to "home", "id" to "cta_buy"))
 * analytics?.screen("ProductDetail", mapOf("productId" to "123"))
 * ```
 */
public class AnalyticsApi internal constructor(
    private val store: AnalyticsStore,
) {
    /**
     * Track a custom event.
     * @param name       Event name, e.g. `"button_tap"`.
     * @param properties Arbitrary key-value metadata.
     * @param source     Optional adapter label (e.g. `"Firebase"`).
     */
    public fun track(
        name: String,
        properties: Map<String, String> = emptyMap(),
        source: String? = null,
    ): Unit =
        store.record(
            AnalyticsEvent(
                id = generateId(),
                name = name,
                properties = properties,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                source = source,
            ),
        )

    /** Convenience shorthand for screen-view events. */
    public fun screen(
        screenName: String,
        properties: Map<String, String> = emptyMap(),
    ): Unit = track("screen_view", properties + mapOf("screen" to screenName))

    /** Clear all recorded events. */
    public fun clear(): Unit = store.clear()
}
