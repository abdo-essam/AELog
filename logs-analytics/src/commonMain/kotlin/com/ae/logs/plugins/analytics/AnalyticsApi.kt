package com.ae.logs.plugins.analytics

import com.ae.logs.plugins.analytics.model.AnalyticsEvent
import com.ae.logs.plugins.analytics.store.AnalyticsStore
import kotlin.time.Clock

/**
 * Public write-only API for [AnalyticsPlugin].
 *
 * ```kotlin
 * val analytics = AELogs.default.getPlugin<AnalyticsPlugin>()?.api
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
     * @param source     Optional adapter label.
     */
    public fun track(
        name: String,
        properties: Map<String, Any> = emptyMap(),
        source: com.ae.logs.plugins.analytics.model.AdapterSource? = null,
    ) {
        if (!com.ae.logs.AELogs.isEnabled) return

        store.record(
            AnalyticsEvent(
                id = generateId(),
                name = name,
                properties = properties,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                source = source,
            ),
        )
    }

    /** Convenience shorthand for screen-view events. */
    public fun screen(
        screenName: String,
        properties: Map<String, Any> = emptyMap(),
    ): Unit = track("screen_view", properties + mapOf("screen" to screenName))

    /** Clear all recorded events. */
    public fun clear(): Unit = store.clear()
}
