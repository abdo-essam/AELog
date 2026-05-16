package com.ae.log.benchmarks

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.config.LogConfig
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
 * JMH benchmarks for the network recording pipeline:
 *
 *   recorder.logRequest(id, url, method, headers, body)
 *     → NetworkStorage.recordOrReplace()
 *       → PluginStorage.addOrReplace()  (lock + indexOfFirst scan + StateFlow)
 *
 *   recorder.logResponse(id, statusCode, body, headers, durationMs)
 *     → NetworkStorage.update()
 *       → PluginStorage.updateFirst()   (lock + indexOfFirst scan + copy + StateFlow)
 *
 * The two-phase request/response model means EVERY network call hits the storage twice.
 * This is the key cost to measure for the network plugin.
 *
 * Run:
 *   ./gradlew :benchmarks:jvmBenchmark --args ".*NetworkPipeline.*"
 */
@OptIn(AELogTestApi::class)
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
open class NetworkPipelineBenchmark {

    private lateinit var plugin: NetworkPlugin

    @Setup
    fun setup() {
        AELog.init(
            NetworkPlugin(maxEntries = 200),
            config = LogConfig(dispatcher = Dispatchers.Unconfined),
        )
        plugin = AELog.getPlugin<NetworkPlugin>()!!
    }

    @TearDown
    fun teardown() {
        AELog.resetForTesting()
    }

    /**
     * Cost of logging a new outgoing request (phase 1 of 2).
     * Creates a pending [NetworkEntry] and inserts it into storage.
     * Target: < 20µs.
     */
    @Benchmark
    fun logRequest() {
        val id = plugin.recorder.newId()
        plugin.recorder.logRequest(
            id = id,
            url = "https://api.example.com/v1/products",
            method = "GET",
            headers = mapOf("Authorization" to "Bearer tok", "Accept" to "application/json"),
        )
    }

    /**
     * Full request → response round-trip (both phases).
     * This is the cost of ONE complete network call being recorded.
     * Target: < 50µs for the combined pair.
     */
    @Benchmark
    fun fullRequestResponsePair() {
        val id = plugin.recorder.newId()
        plugin.recorder.logRequest(
            id = id,
            url = "https://api.example.com/v1/products/42",
            method = "POST",
            headers = mapOf("Content-Type" to "application/json"),
            body = """{"name":"Item","price":9.99}""",
        )
        plugin.recorder.logResponse(
            id = id,
            statusCode = 201,
            body = """{"id":42,"name":"Item","price":9.99}""",
            headers = mapOf("Content-Type" to "application/json"),
            durationMs = 87L,
        )
    }

    /**
     * Cost of [NetworkPlugin.recorder.newId] — UUID generation.
     * Called before every request to obtain a correlation ID.
     * This is purely [IdGenerator.next] = [kotlin.uuid.Uuid.random].
     */
    @Benchmark
    fun newIdGeneration(): String = plugin.recorder.newId()

    /**
     * Cost of patching the response body after the initial response log.
     * Used by Ktor/OkHttp interceptors after reading the full body stream.
     */
    @Benchmark
    fun updateResponseBody() {
        val id = plugin.recorder.newId()
        plugin.recorder.logRequest(id, "https://api.example.com/stream", "GET")
        plugin.recorder.logResponse(id, 200)
        plugin.recorder.updateResponseBody(id, """{"streamed":true,"data":[1,2,3]}""")
    }
}
