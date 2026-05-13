package com.ae.log.plugins.network.storage

import com.ae.log.core.storage.PluginStorage
import com.ae.log.plugins.network.model.NetworkEntry
import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe storage for [NetworkEntry] items backed by [PluginStorage].
 *
 * Uses [PluginStorage]'s [StateFlow] so the UI stays reactive
 * without needing direct Flow subscriptions in the plugin.
 */
internal class NetworkStorage(
    capacity: Int = 200,
) {
    private val storage = PluginStorage<NetworkEntry>(capacity)

    /** Hot stream of all recorded entries, newest first. */
    val entries: StateFlow<List<NetworkEntry>> = storage.dataFlow

    /** Record a new request or replace an existing one by ID (for in-flight updates). */
    fun recordOrReplace(entry: NetworkEntry) {
        storage.addOrReplace({ it.id == entry.id }, entry)
    }

    /** Update an existing entry atomically by ID. No-op if ID is not found. */
    fun update(
        id: String,
        transform: (NetworkEntry) -> NetworkEntry,
    ) {
        storage.updateFirst({ it.id == id }, transform)
    }

    fun clear(): Unit = storage.clear()
}
