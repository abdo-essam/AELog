package com.ae.log.plugins.analytics.storage

import com.ae.log.core.storage.PluginStorage
import com.ae.log.plugins.analytics.model.AnalyticsEvent
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
