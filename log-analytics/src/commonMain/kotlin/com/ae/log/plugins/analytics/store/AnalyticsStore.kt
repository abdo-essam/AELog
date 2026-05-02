package com.ae.log.plugins.analytics.store

import com.ae.log.core.store.PluginStore
import com.ae.log.plugins.analytics.model.AnalyticsEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe storage for [AnalyticsEvent] items backed by [PluginStore].
 */
internal class AnalyticsStore(
    capacity: Int = 500,
) {
    private val store = PluginStore<AnalyticsEvent>(capacity)

    /** Hot stream of all recorded events, oldest first. */
    val events: StateFlow<List<AnalyticsEvent>> = store.dataFlow

    fun record(event: AnalyticsEvent): Unit = store.add(event)

    fun clear(): Unit = store.clear()
}
