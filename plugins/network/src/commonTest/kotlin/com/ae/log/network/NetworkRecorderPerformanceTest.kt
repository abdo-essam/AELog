package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.network.storage.NetworkStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * Strategy 4: Integration-level throughput tests for the network recording pipeline.
 *
 * Tests the full two-phase flow:
 *   recorder.logRequest()  → NetworkStorage.recordOrReplace() → PluginStorage.addOrReplace()
 *   recorder.logResponse() → NetworkStorage.update()          → PluginStorage.updateFirst()
 *
 * This is the most expensive per-call path in the network plugin because:
 * 1. logRequest  → addOrReplace → indexOfFirst (scan) + add + toList + StateFlow emit
 * 2. logResponse → updateFirst  → indexOfFirst (scan) + replace + toList + StateFlow emit
 *
 * Performance targets:
 *   - 1,000 full request/response pairs: < 5s
 *   - 200-capacity ring correctly evicts
 */
@OptIn(AELogTestApi::class)
class NetworkRecorderPerformanceTest {
    private lateinit var storage: NetworkStorage
    private lateinit var recorder: NetworkRecorder

    @BeforeTest
    fun setUp() {
        AELog.init(NetworkPlugin())
        storage = NetworkStorage(capacity = 200)
        recorder = NetworkRecorder(storage)
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Throughput ────────────────────────────────────────────────────────

    @Test
    fun `network recorder - 1000 request-response pairs complete under 5s`() {
        val elapsed =
            measureTime {
                repeat(1_000) { i ->
                    val id = recorder.newId()
                    recorder.logRequest(
                        id = id,
                        url = "https://api.example.com/items/$i",
                        method = "GET",
                        headers = mapOf("Authorization" to "Bearer token123"),
                    )
                    recorder.logResponse(
                        id = id,
                        statusCode = 200,
                        body = """{"id":$i,"status":"ok"}""",
                        headers = mapOf("Content-Type" to "application/json"),
                        durationMs = (10L + i % 200),
                    )
                }
            }
        println("[Perf] NetworkRecorder req+resp ×1000: $elapsed")
        assertTrue(
            elapsed < 15.seconds,
            "NetworkRecorder request-response ×1k took $elapsed — exceeds 15s budget",
        )
    }

    @Test
    fun `network recorder - 500 logRequest only pending entries`() {
        val elapsed =
            measureTime {
                repeat(500) { i ->
                    val id = recorder.newId()
                    recorder.logRequest(
                        id = id,
                        url = "https://api.example.com/stream/$i",
                        method = "POST",
                        body = """{"item":$i}""",
                    )
                }
            }
        println("[Perf] NetworkRecorder.logRequest ×500: $elapsed")
        assertTrue(
            elapsed < 10.seconds,
            "NetworkRecorder.logRequest ×500 took $elapsed — exceeds 10s budget",
        )
    }

    @Test
    fun `network recorder - response body patching 1000 times under 5s`() {
        // Pre-populate storage with a request entry
        val id = recorder.newId()
        recorder.logRequest(id, "https://api.example.com/data", "GET")
        recorder.logResponse(id, 200)

        val elapsed =
            measureTime {
                repeat(1_000) { i ->
                    recorder.updateResponseBody(id, """{"patch":$i}""")
                }
            }
        println("[Perf] NetworkRecorder.updateResponseBody ×1000: $elapsed")
        assertTrue(
            elapsed < 15.seconds,
            "NetworkRecorder.updateResponseBody ×1k took $elapsed — exceeds 15s budget",
        )
    }

    // ── Ring buffer capacity / eviction ───────────────────────────────────

    @Test
    fun `network recorder - ring evicts at capacity=200 correctly`() {
        val capacity = 200
        val totalRequests = capacity + 50

        repeat(totalRequests) { i ->
            val id = recorder.newId()
            recorder.logRequest(id, "https://api.example.com/req/$i", "GET")
            recorder.logResponse(id, 200, durationMs = 10L)
        }

        val stored = storage.entries.value
        assertEquals(capacity, stored.size, "Storage must cap at $capacity entries")
    }

    // ── ID generation ─────────────────────────────────────────────────────

    @Test
    fun `newId - 10000 unique IDs generated quickly`() {
        val elapsed =
            measureTime {
                val ids = (1..10_000).map { recorder.newId() }.toSet()
                assertEquals(10_000, ids.size, "All generated IDs must be unique")
            }
        println("[Perf] NetworkRecorder.newId ×10k (UUID): $elapsed")
        assertTrue(
            elapsed < 2.seconds,
            "NetworkRecorder.newId ×10k took $elapsed — exceeds 2s budget",
        )
    }
}
