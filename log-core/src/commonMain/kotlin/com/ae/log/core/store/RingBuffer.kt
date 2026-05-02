package com.ae.log.core.store

/**
 * Fixed-capacity circular buffer (ring buffer).
 *
 * When full, the oldest item is evicted to make room for the new one.
 * All operations are O(1) amortized.
 *
 * **Not thread-safe on its own.** Wrap calls inside [PluginStore]
 * which uses [kotlinx.atomicfu.locks.synchronized]
 * to guarantee thread safety across all KMP targets.
 *
 * @param capacity Maximum number of items to hold. Must be > 0.
 */
public class RingBuffer<T>(
    public val capacity: Int,
) {
    init {
        require(capacity > 0) { "RingBuffer capacity must be > 0, was $capacity" }
    }

    @Suppress("UNCHECKED_CAST")
    private val buffer: Array<Any?> = arrayOfNulls(capacity)
    private var head = 0 // index of next write slot
    private var size = 0

    /**
     * Add an item. If at capacity, the oldest entry is overwritten.
     */
    public fun add(item: T) {
        buffer[head] = item
        head = (head + 1) % capacity
        if (size < capacity) size++
    }

    /**
     * Returns all items in insertion order (oldest first).
     *
     * **Important:** uses `until` (exclusive) — NOT `..` (inclusive) —
     * to avoid reading the one uninitialized null slot past the last entry.
     * The previous `0..size` caused a NPE crash in `LazyColumn`.
     */
    @Suppress("UNCHECKED_CAST")
    public fun toList(): List<T> {
        if (size == 0) return emptyList()
        val result = ArrayList<T>(size)
        val start = if (size < capacity) 0 else head
        for (i in 0 until size) { // ← was `0..size` (off-by-one bug!)
            result.add(buffer[(start + i) % capacity] as T)
        }
        return result
    }

    /** Remove all items. */
    public fun clear() {
        buffer.fill(null)
        head = 0
        size = 0
    }

    /**
     * Replace the item at the logical [index] (0 is oldest).
     */
    public fun replace(
        index: Int,
        item: T,
    ) {
        if (index !in 0 until size) throw IndexOutOfBoundsException("Index $index out of bounds for size $size")
        val start = if (size < capacity) 0 else head
        val realIndex = (start + index) % capacity
        buffer[realIndex] = item
    }

    /** Current number of stored items. */
    public val count: Int get() = size

    /** `true` when no items are stored. */
    public val isEmpty: Boolean get() = size == 0
}
