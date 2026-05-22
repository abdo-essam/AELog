package com.ae.log.storage

import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe, reactive storage contract for plugin data.
 *
 * [dataFlow] is the single source of truth.
 * All mutations atomically emit a new immutable snapshot.
 */
public interface PluginStorage<T> {

    /** Current snapshot of stored items. Emits a new list after every mutation. */
    public val dataFlow: StateFlow<List<T>>

    /** Append [item]. Capacity-bounded implementations may evict oldest items. */
    public fun add(item: T)

    /** Remove all stored items. */
    public fun clear()
}
