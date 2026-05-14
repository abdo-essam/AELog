package com.ae.log.analytics.storage

import com.ae.log.analytics.model.AnalyticsEvent
import com.ae.log.storage.PluginStorage
import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe storage for [AnalyticsEvent] items backed by [PluginStorage].
 */
internal class AnalyticsStorage(
    capacity: Int = 500,
) {
    private val storage = PluginStorage<AnalyticsEvent>(capacity)

    /** Hot stream of all recorded events, oldest first. */
    val events: StateFlow<List<AnalyticsEvent>> = storage.dataFlow

    fun record(event: AnalyticsEvent): Unit = storage.add(event)

    fun clear(): Unit = storage.clear()
}
