package com.ae.log.storage

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Capacity-bounded, in-memory [PluginStorage].
 *
 * Evicts oldest items when [capacity] is exceeded.
 * [dataFlow] holds the data directly — no backing collection.
 *
 * @param capacity maximum number of items to retain. Must be > 0.
 */
public class InMemoryPluginStorage<T>(
    private val capacity: Int,
) : PluginStorage<T> {
    init {
        require(capacity > 0) { "Capacity must be > 0, was $capacity" }
    }

    private val lock = SynchronizedObject()
    private val _dataFlow = MutableStateFlow<List<T>>(emptyList())
    override val dataFlow: StateFlow<List<T>> = _dataFlow.asStateFlow()

    override fun add(item: T) {
        synchronized(lock) {
            val current = _dataFlow.value
            _dataFlow.value =
                if (current.size >= capacity) {
                    current.drop(1) + item
                } else {
                    current + item
                }
        }
    }

    override fun clear() {
        synchronized(lock) {
            _dataFlow.value = emptyList()
        }
    }

    /** Imports a list of items into this storage, keeping only up to [capacity] newest items. */
    public fun import(items: List<T>) {
        synchronized(lock) {
            val combined = _dataFlow.value + items
            _dataFlow.value =
                if (combined.size > capacity) {
                    combined.takeLast(capacity)
                } else {
                    combined
                }
        }
    }

    /** Atomically find and update the first matching element. No-op if not found. */
    public fun updateFirst(
        predicate: (T) -> Boolean,
        transform: (T) -> T,
    ) {
        synchronized(lock) {
            val current = _dataFlow.value
            val index = current.indexOfFirst(predicate)
            if (index == -1) return
            _dataFlow.value =
                current.toMutableList().apply {
                    this[index] = transform(this[index])
                }
        }
    }

    /** Add [item], or replace the first match if [predicate] hits. */
    public fun addOrReplace(
        predicate: (T) -> Boolean,
        item: T,
    ) {
        synchronized(lock) {
            val current = _dataFlow.value
            val index = current.indexOfFirst(predicate)
            _dataFlow.value =
                if (index == -1) {
                    if (current.size >= capacity) {
                        current.drop(1) + item
                    } else {
                        current + item
                    }
                } else {
                    current.toMutableList().apply { this[index] = item }
                }
        }
    }
}
