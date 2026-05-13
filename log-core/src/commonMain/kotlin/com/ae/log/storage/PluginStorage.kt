package com.ae.log.storage

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

public class PluginStorage<T>(
    capacity: Int,
) : SynchronizedObject() {
    private val ring = RingBuffer<T>(capacity)
    private val _dataFlow = MutableStateFlow<List<T>>(emptyList())
    public val dataFlow: StateFlow<List<T>> = _dataFlow.asStateFlow()

    public fun add(item: T) {
        synchronized(this) {
            ring.add(item)
            _dataFlow.value = ring.toList()
        }
    }

    public fun clear() {
        synchronized(this) {
            ring.clear()
            _dataFlow.value = emptyList()
        }
    }

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

    public fun updateFirst(
        predicate: (T) -> Boolean,
        transform: (T) -> T,
    ) {
        synchronized(this) {
            val index = _dataFlow.value.indexOfFirst(predicate)
            if (index == -1) return
            ring.replace(index, transform(_dataFlow.value[index]))
            _dataFlow.value = ring.toList()
        }
    }

    public fun addOrReplace(
        predicate: (T) -> Boolean,
        item: T,
    ) {
        synchronized(this) {
            val index = _dataFlow.value.indexOfFirst(predicate)
            if (index == -1) ring.add(item) else ring.replace(index, item)
            _dataFlow.value = ring.toList()
        }
    }

    public val count: Int get() = _dataFlow.value.size
    public val isEmpty: Boolean get() = _dataFlow.value.isEmpty()
}
