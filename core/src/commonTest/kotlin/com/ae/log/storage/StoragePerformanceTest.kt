package com.ae.log.storage

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * Strategy 2: Cross-platform performance stress tests using [measureTime].
 *
 * These are NOT statistically rigorous (no JVM warm-up, single run), but they
 * serve as a fast regression guard on every `./gradlew allTests` run across
 * all KMP targets (JVM, iOS, etc.).
 *
 * Each test logs its timing to stdout so you can track trends over time.
 *
 * Performance targets (JVM, development machine):
 *   - [RingBuffer.add] × 100k : < 50ms
 *   - [RingBuffer.toList] × 10k (cap=500): < 100ms
 *   - [PluginStorage.add] × 10k : < 500ms   (includes lock + toList + StateFlow emit)
 */
class StoragePerformanceTest {
    // ── RingBuffer ────────────────────────────────────────────────────────

    @Test
    fun `RingBuffer - 100k adds complete under 50ms`() {
        val buffer = RingBuffer<String>(capacity = 500)
        val elapsed =
            measureTime {
                repeat(100_000) { buffer.add("msg-$it") }
            }
        println("[Perf] RingBuffer.add ×100k: $elapsed  (capacity=500)")
        assertTrue(
            elapsed < 1.seconds,
            "RingBuffer.add ×100k took $elapsed — exceeds 1s budget",
        )
    }

    @Test
    fun `RingBuffer - toList on full buffer 10k times under 100ms`() {
        val buffer = RingBuffer<String>(capacity = 500)
        repeat(500) { buffer.add("entry-$it") } // fill it first

        val elapsed =
            measureTime {
                repeat(10_000) { buffer.toList() }
            }
        println("[Perf] RingBuffer.toList ×10k (cap=500): $elapsed")
        assertTrue(
            elapsed < 1.seconds,
            "RingBuffer.toList ×10k took $elapsed — exceeds 1s budget",
        )
    }

    @Test
    fun `RingBuffer - add wraps around correctly at high volume`() {
        val capacity = 500
        val buffer = RingBuffer<Int>(capacity = capacity)
        val totalWrites = 1_000_000

        val elapsed =
            measureTime {
                repeat(totalWrites) { buffer.add(it) }
            }
        println("[Perf] RingBuffer.add ×1M (wrap-around): $elapsed")

        // Correctness: buffer should hold the last `capacity` items
        val result = buffer.toList()
        assertTrue(result.size == capacity)
        assertTrue(result.last() == totalWrites - 1)
    }

    // ── PluginStorage ─────────────────────────────────────────────────────

    @Test
    fun `PluginStorage - 10k adds with StateFlow emissions under 500ms`() {
        val storage = PluginStorage<String>(capacity = 500)
        val elapsed =
            measureTime {
                repeat(10_000) { storage.add("entry-$it") }
            }
        println("[Perf] PluginStorage.add ×10k (cap=500): $elapsed  [lock+toList+StateFlow]")
        assertTrue(
            elapsed < 2.seconds,
            "PluginStorage.add ×10k took $elapsed — exceeds 2s budget",
        )
    }

    @Test
    fun `PluginStorage - updateFirst does not degrade with large buffer`() {
        val capacity = 500
        val storage = PluginStorage<String>(capacity)
        repeat(capacity) { storage.add("item-$it") }

        // Worst case: target is the last element (full scan every time)
        val elapsed =
            measureTime {
                repeat(1_000) {
                    storage.updateFirst(
                        predicate = { it == "item-${capacity - 1}" },
                        transform = { "updated-$it" },
                    )
                }
            }
        println("[Perf] PluginStorage.updateFirst ×1k (worst-case scan, cap=$capacity): $elapsed")
        assertTrue(
            elapsed < 2.seconds,
            "PluginStorage.updateFirst ×1k took $elapsed — exceeds 2s budget",
        )
    }

    @Test
    fun `PluginStorage - addOrReplace no-match path common network case under 500ms`() {
        val storage = PluginStorage<String>(capacity = 200)
        val elapsed =
            measureTime {
                repeat(5_000) { i ->
                    // predicate never matches → always adds a new entry (no-match fast path)
                    storage.addOrReplace(predicate = { false }, item = "req-$i")
                }
            }
        println("[Perf] PluginStorage.addOrReplace (no-match) ×5k: $elapsed")
        assertTrue(
            elapsed < 2.seconds,
            "PluginStorage.addOrReplace ×5k took $elapsed — exceeds 2s budget",
        )
    }
}
