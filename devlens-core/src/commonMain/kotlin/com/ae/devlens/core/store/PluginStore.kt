package com.ae.devlens.core.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Reactive, fixed-capacity data store for plugin data.
 *
 * Wraps a [RingBuffer] with a [StateFlow] so UI can observe changes.
 * Thread-safe for concurrent writes via [kotlinx.coroutines.flow.MutableStateFlow.update].
 *
 * ```kotlin
 * class MyPlugin : DataPlugin {
 *     private val store = PluginStore<MyEvent>(capacity = 200)
 *
 *     override fun onAttach(context: PluginContext) {
 *         context.scope.launch {
 *             store.dataFlow.collect { events -> updateBadge(events.size) }
 *         }
 *     }
 *
 *     fun record(event: MyEvent) = store.add(event)
 * }
 * ```
 *
 * @param T Type of data stored.
 * @param capacity Maximum number of items; older items are evicted when full.
 */
public class PluginStore<T>(
    capacity: Int,
) {
    private val ring = RingBuffer<T>(capacity)

    // MutableStateFlow<List<T>> is the reactive wrapper.
    // We snapshot the ring buffer on every mutation so collectors always see
    // an immutable list — no defensive copies needed on the read side.
    private val _dataFlow = MutableStateFlow<List<T>>(emptyList())

    /** Reactive, read-only view of all stored items (oldest first). */
    public val dataFlow: StateFlow<List<T>> = _dataFlow.asStateFlow()

    /**
     * Add an item to the store and emit the updated list.
     *
     * If the store is at capacity the oldest item is silently evicted.
     */
    public fun add(item: T) {
        _dataFlow.update {
            ring.add(item)
            ring.toList()
        }
    }

    /** Remove all items and emit an empty list. */
    public fun clear() {
        _dataFlow.update {
            ring.clear()
            emptyList()
        }
    }

    /**
     * Replace the item at [index] with [item] and emit the updated list.
     * No-op if [index] is out of bounds.
     */
    public fun replace(
        index: Int,
        item: T,
    ) {
        _dataFlow.update { current ->
            if (index !in current.indices) return@update current
            ring.clear()
            val updated = current.toMutableList().also { it[index] = item }
            updated.forEach { ring.add(it) }
            ring.toList()
        }
    }

    /** Current number of items. */
    public val count: Int get() = ring.count

    /** True when the store holds no items. */
    public val isEmpty: Boolean get() = ring.isEmpty
}
