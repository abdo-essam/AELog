package com.ae.log.benchmarks

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.config.LogConfig
import com.ae.log.logs.LogPlugin
import com.ae.log.logs.PlatformLogSink
import com.ae.log.logs.log
import com.ae.log.network.NetworkPlugin
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.TearDown
import kotlinx.benchmark.Warmup
import kotlinx.coroutines.Dispatchers

/**
 * JMH benchmarks for the full AELog log pipeline:
 *
 *   AELog.log.d(tag, msg)
 *     → LogProxy.record()
 *       → AELog.getPlugin<LogPlugin>()    (list scan + isInstance)
 *         → LogPlugin.record()
 *           → LogRecorder.log()           (severity filter + clock + id)
 *             → PluginStorage.add()       (lock + RingBuffer + StateFlow)
 *
 * [logWithExplicitTag] shows the cost of the entire chain WITHOUT stack capture.
 * [logWithCallerTag]   adds the cost of [callerTag()], which captures a stack trace.
 * [getPluginLookup]    isolates the [PluginManager.getPlugin] scan in isolation.
 *
 * Run:
 *   ./gradlew :benchmarks:jvmBenchmark --args ".*LogPipeline.*"
 */
@OptIn(AELogTestApi::class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.NANOSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
open class LogPipelineBenchmark {

    @Setup
    fun setup() {
        AELog.init(
            // PlatformLogSink.None avoids Logcat/println side-effects in the benchmark
            LogPlugin(platformLogSink = PlatformLogSink.None),
            NetworkPlugin(),
            config = LogConfig(dispatcher = Dispatchers.Unconfined),
        )
    }

    @TearDown
    fun teardown() {
        AELog.resetForTesting()
    }

    /**
     * End-to-end cost of one log call with an explicitly provided tag.
     * This is the FASTEST path — no stack trace capture.
     * Target: < 10µs on JVM.
     */
    @Benchmark
    fun logWithExplicitTag() {
        AELog.log.d("BenchTag", "Hello benchmark message — explicit tag path")
    }

    /**
     * End-to-end cost of one log call WITHOUT a tag — auto-tag via [callerTag()].
     * Captures a Throwable and parses its stack trace string. This is expensive.
     * Target: < 100µs on JVM. Shows callerTag overhead vs [logWithExplicitTag].
     */
    @Benchmark
    fun logWithCallerTag() {
        AELog.log.d("Hello benchmark message — callerTag path")
    }

    /**
     * Cost of the isEnabled guard — the fast-path exit point.
     * When [AELog.isEnabled] is false, the call should return immediately
     * after a single atomic read.
     * Target: < 50ns.
     */
    @Benchmark
    fun logWhenDisabled() {
        AELog.isEnabled = false
        AELog.log.d("BenchTag", "Should be skipped immediately")
        AELog.isEnabled = true
    }

    /**
     * Isolates the cost of [PluginManager.getPlugin] — a list scan with
     * KClass.isInstance check. Called on every single log invocation via LogProxy.
     */
    @Benchmark
    fun getPluginLookup(): LogPlugin? = AELog.getPlugin<LogPlugin>()

    /**
     * Cost of [AELog.export] on a full log buffer.
     * Used for exporting all log data (e.g., crash reports).
     * Target: < 5ms for 500 entries.
     */
    @Benchmark
    fun export(): String = AELog.export()
}
