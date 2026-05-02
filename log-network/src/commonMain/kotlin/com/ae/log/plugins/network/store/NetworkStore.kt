package com.ae.log.plugins.network.store

import com.ae.log.core.store.PluginStore
import com.ae.log.plugins.network.model.NetworkEntry
import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe storage for [NetworkEntry] items backed by [PluginStore].
 *
 * Uses [PluginStore]'s [StateFlow] so the UI stays reactive
 * without needing direct Flow subscriptions in the plugin.
 */
internal class NetworkStore(
    capacity: Int = 200,
) {
    private val store = PluginStore<NetworkEntry>(capacity)

    /** Hot stream of all recorded entries, newest first. */
    val entries: StateFlow<List<NetworkEntry>> = store.dataFlow

    /** Record a new request or replace an existing one by ID (for in-flight updates). */
    fun recordOrReplace(entry: NetworkEntry) {
        store.addOrReplace({ it.id == entry.id }, entry)
    }

    /** Update an existing entry atomically by ID. No-op if ID is not found. */
    fun update(
        id: String,
        transform: (NetworkEntry) -> NetworkEntry,
    ) {
        store.updateFirst({ it.id == id }, transform)
    }

    fun clear(): Unit = store.clear()
}
