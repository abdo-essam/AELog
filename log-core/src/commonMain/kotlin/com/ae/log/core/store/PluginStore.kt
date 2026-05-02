package com.ae.log.core.store

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reactive, fixed-capacity data store for plugin data.
 *
 * Wraps a [RingBuffer] with a [StateFlow] so UI can observe changes.
 * Thread-safe for concurrent writes using a lock.
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
) : SynchronizedObject() {
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
        synchronized(this) {
            ring.add(item)
            _dataFlow.value = ring.toList()
        }
    }

    /** Remove all items and emit an empty list. */
    public fun clear() {
        synchronized(this) {
            ring.clear()
            _dataFlow.value = emptyList()
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
        synchronized(this) {
            if (index !in 0 until ring.count) return
            ring.replace(index, item)
            _dataFlow.value = ring.toList()
        }
    }

    /**
     * Atomically find an item using [predicate] and replace it with the result of [transform].
     * No-op if no item matches the predicate.
     */
    public fun updateFirst(
        predicate: (T) -> Boolean,
        transform: (T) -> T,
    ) {
        synchronized(this) {
            val current = _dataFlow.value
            val index = current.indexOfFirst(predicate)
            if (index == -1) return

            ring.replace(index, transform(current[index]))
            _dataFlow.value = ring.toList()
        }
    }

    /**
     * Atomically find an item using [predicate] and replace it with [item].
     * If not found, add the [item].
     */
    public fun addOrReplace(
        predicate: (T) -> Boolean,
        item: T,
    ) {
        synchronized(this) {
            val current = _dataFlow.value
            val index = current.indexOfFirst(predicate)
            if (index == -1) {
                ring.add(item)
            } else {
                ring.replace(index, item)
            }
            _dataFlow.value = ring.toList()
        }
    }

    /** Current number of items. */
    public val count: Int get() = synchronized(this) { ring.count }

    /** True when the store holds no items. */
    public val isEmpty: Boolean get() = synchronized(this) { ring.isEmpty }
}
