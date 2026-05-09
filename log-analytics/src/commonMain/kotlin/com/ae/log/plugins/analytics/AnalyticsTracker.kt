package com.ae.log.plugins.analytics

import com.ae.log.AELog
import com.ae.log.core.utils.IdGenerator
import com.ae.log.plugins.analytics.model.AdapterSource
import com.ae.log.plugins.analytics.model.AnalyticsEvent
import com.ae.log.plugins.analytics.store.AnalyticsStore
import kotlin.time.Clock

/**
 * Public write-only API for [AnalyticsPlugin].
 *
 * ```kotlin
 * // Preferred: via the analytics proxy
 * AELog.analytics.logEvent("button_tap", mapOf("screen" to "home", "id" to "cta_buy"))
 * AELog.analytics.logScreen("ProductDetail", mapOf("productId" to "123"))
 *
 * // Advanced: direct plugin access
 * AELog.getPlugin<AnalyticsPlugin>()?.tracker?.track("button_tap")
 * ```
 */
public class AnalyticsTracker internal constructor(
    private val store: AnalyticsStore,
    private val clock: Clock = Clock.System,
    private val idGenerator: () -> String = { IdGenerator.next() },
) {
    /**
     * Track a custom event.
     * @param name       Event name, e.g. `"button_tap"`.
     * @param properties Key-value metadata. Values are passed as [String].
     * @param source     Optional adapter label.
     */
    public fun track(
        name: String,
        properties: Map<String, String> = emptyMap(),
        source: AdapterSource? = null,
    ) {
        if (!AELog.isEnabled) return

        store.record(
            AnalyticsEvent(
                id = idGenerator(),
                name = name,
                properties = properties,
                timestamp = clock.now().toEpochMilliseconds(),
                source = source,
            ),
        )
    }

    /** Convenience shorthand for screen-view events. */
    public fun screen(
        screenName: String,
        properties: Map<String, String> = emptyMap(),
    ): Unit = track("screen_view", properties + mapOf("screen" to screenName))

    /** Clear all recorded events. */
    public fun clear(): Unit = store.clear()
}
