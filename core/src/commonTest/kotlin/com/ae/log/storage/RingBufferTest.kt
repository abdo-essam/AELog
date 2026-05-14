package com.ae.log.storage

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for [RingBuffer] which is `internal`. These tests run in the same
 * module (commonTest) so they have access to the internal class directly.
 */
class RingBufferTest {

    @Test
    fun `add - fills buffer in order`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        buffer.add(1)
        buffer.add(2)
        buffer.add(3)
        assertEquals(listOf(1, 2, 3), buffer.toList())
    }

    @Test
    fun `add - overwrites oldest element when capacity exceeded`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        buffer.add(1)
        buffer.add(2)
        buffer.add(3)
        buffer.add(4) // wraps — evicts 1
        assertEquals(listOf(2, 3, 4), buffer.toList())
    }

    @Test
    fun `add - with capacity 1 always holds last element`() {
        val buffer = RingBuffer<Int>(capacity = 1)
        buffer.add(10)
        buffer.add(20)
        buffer.add(30)
        assertEquals(listOf(30), buffer.toList())
    }

    @Test
    fun `add - large overflow sequence is correct`() {
        val buffer = RingBuffer<Int>(capacity = 4)
        (1..10).forEach { buffer.add(it) }
        assertEquals(listOf(7, 8, 9, 10), buffer.toList())
    }

    @Test
    fun `toList - returns empty list when empty`() {
        val buffer = RingBuffer<String>(capacity = 5)
        assertTrue(buffer.toList().isEmpty())
    }

    @Test
    fun `clear - resets count and toList`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        buffer.add(1)
        buffer.add(2)
        buffer.clear()
        assertTrue(buffer.toList().isEmpty())
        assertEquals(0, buffer.count)
    }

    @Test
    fun `replace - updates element at given index`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        buffer.add(10)
        buffer.add(20)
        buffer.add(30)
        buffer.replace(index = 1, item = 99)
        assertEquals(listOf(10, 99, 30), buffer.toList())
    }

    @Test
    fun `replace - throws when index is out of bounds`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        buffer.add(1)
        assertFailsWith<IndexOutOfBoundsException> {
            buffer.replace(index = 5, item = 42)
        }
    }

    @Test
    fun `replace - works correctly after overflow`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        buffer.add(1); buffer.add(2); buffer.add(3); buffer.add(4) // list is [2,3,4]
        buffer.replace(index = 0, item = 99)
        assertEquals(listOf(99, 3, 4), buffer.toList())
    }

    @Test
    fun `count - tracks size correctly`() {
        val buffer = RingBuffer<Int>(capacity = 3)
        assertEquals(0, buffer.count)
        buffer.add(1)
        assertEquals(1, buffer.count)
        buffer.add(2)
        buffer.add(3)
        assertEquals(3, buffer.count)
        buffer.add(4) // overflow — count stays at capacity
        assertEquals(3, buffer.count)
    }

    @Test
    fun `constructor - throws on non-positive capacity`() {
        assertFailsWith<IllegalArgumentException> {
            RingBuffer<Int>(capacity = 0)
        }
        assertFailsWith<IllegalArgumentException> {
            RingBuffer<Int>(capacity = -1)
        }
    }
}
