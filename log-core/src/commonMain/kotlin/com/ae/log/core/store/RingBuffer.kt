package com.ae.log.core.store

public class RingBuffer<T>(public val capacity: Int) {
    init { require(capacity > 0) { "RingBuffer capacity must be > 0, was $capacity" } }

    @Suppress("UNCHECKED_CAST")
    private val buffer: Array<Any?> = arrayOfNulls(capacity)
    private var head = 0
    private var size = 0

    public fun add(item: T) { buffer[head] = item; head = (head + 1) % capacity; if (size < capacity) size++ }

    @Suppress("UNCHECKED_CAST")
    public fun toList(): List<T> {
        if (size == 0) return emptyList()
        val result = ArrayList<T>(size)
        val start = if (size < capacity) 0 else head
        for (i in 0 until size) result.add(buffer[(start + i) % capacity] as T)
        return result
    }

    public fun clear() { buffer.fill(null); head = 0; size = 0 }

    public fun replace(index: Int, item: T) {
        if (index !in 0 until size) throw IndexOutOfBoundsException("Index $index out of bounds for size $size")
        val start = if (size < capacity) 0 else head
        buffer[(start + index) % capacity] = item
    }

    public val count: Int get() = size
    public val isEmpty: Boolean get() = size == 0
}
