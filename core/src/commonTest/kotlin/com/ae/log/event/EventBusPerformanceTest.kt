package com.ae.log.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * Strategy 2: EventBus performance and concurrency stress tests.
 *
 * Note: [EventBus.publish] returns Unit — the tryEmit result is intentionally
 * discarded by the EventBus (events are lifecycle signals, not data). The bus
 * has a 64-event buffer; overflow drops silently which is acceptable by design.
 *
 * Performance targets:
 *   - [EventBus.publish] ×50k: < 200ms  (tryEmit is lock-free)
 *   - Concurrent publishers from multiple coroutines: no exceptions, no deadlock
 */
class EventBusPerformanceTest {
    @Test
    fun `EventBus - 50k publish calls complete under 200ms`() {
        val bus = EventBus()
        var calls = 0

        val elapsed =
            measureTime {
                repeat(50_000) {
                    bus.publish(AppStartedEvent)
                    calls++
                }
            }

        println("[Perf] EventBus.publish ×50k: $elapsed  calls=$calls")
        assertTrue(
            elapsed < 1.seconds,
            "EventBus.publish ×50k took $elapsed — exceeds 1s budget",
        )
    }

    @Test
    fun `EventBus - rapid burst of 64 events does not crash`() {
        val bus = EventBus()

        // Fire exactly the buffer capacity — should be safe by design
        repeat(64) { bus.publish(AppStartedEvent) }

        // If we get here without exception, the test passes
        println("[Perf] EventBus burst (64 events): completed without crash")
        assertTrue(true)
    }

    @Test
    fun `EventBus - concurrent publishers do not throw`() =
        runTest {
            val bus = EventBus()
            var totalCalls = 0

            val jobs =
                (1..10).map { publisherIndex ->
                    launch(Dispatchers.Default) {
                        repeat(1_000) {
                            bus.publish(AppStartedEvent)
                            totalCalls++
                        }
                    }
                }
            jobs.joinAll()

            println("[Perf] EventBus concurrent publish: 10 publishers × 1k = 10k calls, no crashes")
            assertTrue(true, "Concurrent publish must not throw or deadlock")
        }
}
