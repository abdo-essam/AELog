package com.ae.devlens.plugins.network.store

import com.ae.devlens.core.store.PluginStore
import com.ae.devlens.plugins.network.model.NetworkEntry
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
    fun record(entry: NetworkEntry) {
        val current = store.dataFlow.value
        val idx = current.indexOfFirst { it.id == entry.id }
        if (idx == -1) {
            store.add(entry)
        } else {
            // Replace existing entry (e.g. request → response received)
            store.replace(idx, entry)
        }
    }

    fun clear(): Unit = store.clear()
}
