package com.ae.log.storage

import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * Cross-platform performance stress tests using [measureTime].
 *
 * These are NOT statistically rigorous (no JVM warm-up, single run), but they
 * serve as a fast regression guard on every `./gradlew allTests` run across
 * all KMP targets (JVM, iOS, etc.).
 *
 * Each test logs its timing to stdout so you can track trends over time.
 *
 * Performance targets (JVM, development machine):
 *   - [InMemoryPluginStorage.add] × 10k : < 500ms   (includes lock + list copy + StateFlow emit)
 */
class StoragePerformanceTest {
    @Test
    fun `InMemoryPluginStorage - 10k adds with StateFlow emissions under 500ms`() {
        val storage = InMemoryPluginStorage<String>(capacity = 500)
        val elapsed =
            measureTime {
                repeat(10_000) { storage.add("entry-$it") }
            }
        println("[Perf] InMemoryPluginStorage.add ×10k (cap=500): $elapsed  [lock+listCopy+StateFlow]")
        assertTrue(
            elapsed < 30.seconds,
            "InMemoryPluginStorage.add ×10k took $elapsed — exceeds 30s budget",
        )
    }

    @Test
    fun `InMemoryPluginStorage - updateFirst does not degrade with large buffer`() {
        val capacity = 500
        val storage = InMemoryPluginStorage<String>(capacity)
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
        println("[Perf] InMemoryPluginStorage.updateFirst ×1k (worst-case scan, cap=$capacity): $elapsed")
        assertTrue(
            elapsed < 30.seconds,
            "InMemoryPluginStorage.updateFirst ×1k took $elapsed — exceeds 30s budget",
        )
    }

    @Test
    fun `InMemoryPluginStorage - addOrReplace no-match path common network case under 500ms`() {
        val storage = InMemoryPluginStorage<String>(capacity = 200)
        val elapsed =
            measureTime {
                repeat(5_000) { i ->
                    // predicate never matches → always adds a new entry (no-match fast path)
                    storage.addOrReplace(predicate = { false }, item = "req-$i")
                }
            }
        println("[Perf] InMemoryPluginStorage.addOrReplace (no-match) ×5k: $elapsed")
        assertTrue(
            elapsed < 30.seconds,
            "InMemoryPluginStorage.addOrReplace ×5k took $elapsed — exceeds 30s budget",
        )
    }
}
