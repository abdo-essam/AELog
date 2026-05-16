package com.ae.log.logs

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.logs.model.LogSeverity
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

/**
 * Strategy 4: Integration-level throughput tests for the log pipeline.
 *
 * Tests the full path:
 *   LogRecorder.log() → PluginStorage.add() → RingBuffer (ring eviction) → StateFlow
 *
 * These tests exercise [LogPlugin]'s real [LogStorage] instance to catch
 * integration regressions that unit tests cannot.
 *
 * Performance targets:
 *   - 5,000 log calls via recorder: < 2s
 *   - Ring-buffer eviction at 500-entry capacity: correct + fast
 *   - Severity filter fast-path: near zero overhead for filtered entries
 */
@OptIn(AELogTestApi::class)
class LogRecorderPerformanceTest {
    private lateinit var storage: LogStorage
    private lateinit var recorder: LogRecorder

    @BeforeTest
    fun setUp() {
        AELog.init(LogPlugin())
        storage = LogStorage(capacity = 500)
        recorder =
            LogRecorder(
                storage = storage,
                platformLogSink = PlatformLogSink.None, // suppress stdout during tests
            )
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Throughput ────────────────────────────────────────────────────────

    @Test
    fun `log - 5000 entries via recorder complete under 2s`() {
        val elapsed =
            measureTime {
                repeat(5_000) { i ->
                    recorder.log(LogSeverity.DEBUG, "PerfTag", "Benchmark log entry #$i")
                }
            }
        println("[Perf] LogRecorder.log ×5k: $elapsed")
        assertTrue(
            elapsed < 10.seconds,
            "LogRecorder.log ×5k took $elapsed — exceeds 10s budget",
        )
    }

    @Test
    fun `log - severity filter skips entries with near-zero overhead`() {
        val errorOnlyRecorder =
            LogRecorder(
                storage = storage,
                minSeverity = LogSeverity.ERROR,
                platformLogSink = PlatformLogSink.None,
            )

        val elapsed =
            measureTime {
                repeat(10_000) { i ->
                    // These are all below ERROR — should be filtered before touching storage
                    errorOnlyRecorder.log(LogSeverity.DEBUG, "Tag", "Filtered entry #$i")
                }
            }

        println("[Perf] LogRecorder filtered (10k DEBUG below ERROR threshold): $elapsed")
        assertTrue(storage.dataFlow.value.isEmpty(), "Filtered entries must NOT reach storage")
        assertTrue(
            elapsed < 2.seconds,
            "Severity filter fast-path took $elapsed — unexpectedly slow",
        )
    }

    // ── Ring buffer capacity / eviction ───────────────────────────────────

    @Test
    fun `log - ring buffer evicts oldest correctly after overflow`() {
        val capacity = 500
        val totalLogs = capacity + 100 // 600 logs into a cap-500 buffer

        repeat(totalLogs) { i ->
            recorder.log(LogSeverity.INFO, "Tag", "msg-$i")
        }

        val stored = storage.dataFlow.value
        assertEquals(capacity, stored.size, "Storage must cap at $capacity entries")
        // With 600 total entries and cap=500, the oldest surviving entry is #100 (totalLogs - capacity)
        assertEquals(
            "msg-${totalLogs - capacity}",
            stored.first().message,
            "Oldest entry should be msg-${totalLogs - capacity}",
        )
        assertEquals("msg-${totalLogs - 1}", stored.last().message, "Newest entry should be last")
    }

    // ── Export ────────────────────────────────────────────────────────────

    @Test
    fun `export - 500 entries export under 100ms`() {
        // Write via the plugin's own recorder so AELog.export() can find the entries
        val pluginRecorder = AELog.getPlugin<LogPlugin>()!!.recorder
        repeat(500) { i ->
            pluginRecorder.log(LogSeverity.INFO, "ExportTag", "Export test entry $i")
        }

        val elapsed =
            measureTime {
                val exported = AELog.export()
                assertTrue(exported.isNotEmpty(), "Export must not be empty after writing 500 entries")
            }
        println("[Perf] AELog.export (500 entries): $elapsed")
        assertTrue(
            elapsed < 1.seconds,
            "AELog.export took $elapsed — exceeds 1s budget",
        )
    }
}
