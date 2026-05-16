package com.ae.log.benchmarks

import com.ae.log.storage.PluginStorage
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Measurement
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup

/**
 * JMH benchmarks for storage throughput, measured through the public [PluginStorage] API.
 *
 * [RingBuffer] is `internal` and not accessible from outside the `:core` module.
 * Since [PluginStorage] wraps it with a lock + StateFlow emit, these benchmarks
 * measure the combined cost — which is what callers actually pay in production.
 *
 * Key findings to look for:
 * - [addSingle] is the cost of ONE log or network entry being recorded.
 * - [readSnapshot] shows the zero-copy read that the UI performs.
 * - [addThenRead] is the combined write + UI-read round-trip cost.
 * - [updateFirst] shows the O(n) scan cost for network response matching.
 *
 * Note: For raw [RingBuffer] microbenchmarks (isolated from StateFlow overhead),
 * see `StoragePerformanceTest` in `:core:commonTest` — those tests live in the
 * same module as [RingBuffer] and can access it directly via `internal`.
 *
 * Run:
 *   ./gradlew :benchmarks:jvmBenchmark
 *   ./gradlew :benchmarks:jvmBenchmark --args ".*StorageBenchmark.*"
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10, time = 1, timeUnit = BenchmarkTimeUnit.SECONDS)
open class StorageBenchmark {

    @Param("100", "500", "1000")
    var capacity: Int = 0

    private lateinit var storage: PluginStorage<String>

    @Setup
    fun setup() {
        storage = PluginStorage(capacity)
        // Pre-fill to steady-state so we benchmark the overwrite (wrap-around) path
        repeat(capacity) { storage.add("init-$it") }
    }

    /**
     * Cost of one [PluginStorage.add]:
     *   lock + RingBuffer.add + RingBuffer.toList (O(n)) + StateFlow emit.
     *
     * This is the hot path for EVERY log call in the library.
     * Target: < 20µs at capacity=500.
     */
    @Benchmark
    fun addSingle(): PluginStorage<String> {
        storage.add("benchmark-entry")
        return storage
    }

    /**
     * Cost of reading the current snapshot from [PluginStorage.dataFlow].
     * This is what the Compose UI does on every recomposition. No lock needed.
     * Target: < 100ns (atomic read of a reference).
     */
    @Benchmark
    fun readSnapshot(): List<String> = storage.dataFlow.value

    /**
     * Combined write + read: simulates one producer log call followed by
     * one UI consumer read. Represents the worst-case recomposition trigger.
     */
    @Benchmark
    fun addThenRead(): List<String> {
        storage.add("msg")
        return storage.dataFlow.value
    }

    /**
     * [PluginStorage.updateFirst] — used by [NetworkStorage] to match a pending
     * request by ID and merge in the response data. The predicate performs an
     * O(n) scan, making this more expensive than [addSingle].
     *
     * Worst-case: target is the last element. This is the benchmark scenario.
     */
    @Benchmark
    fun updateFirstWorstCase(): PluginStorage<String> {
        val target = "init-${capacity - 1}"
        storage.updateFirst(
            predicate = { it == target || it == "updated" },
            transform = { "updated" },
        )
        return storage
    }

    /**
     * [PluginStorage.addOrReplace] when the predicate does NOT match.
     * This is the common case for [NetworkStorage.recordOrReplace] when a new
     * (never-seen) request ID arrives. Falls through to a plain [add].
     */
    @Benchmark
    fun addOrReplaceNewEntry(): PluginStorage<String> {
        storage.addOrReplace(
            predicate = { false }, // never matches → always adds
            item = "new-entry",
        )
        return storage
    }

    /**
     * [PluginStorage.addOrReplace] when the predicate MATCHES the first element.
     * Best-case scan (O(1) match). Used when a network response completes a
     * pending request that was just added.
     */
    @Benchmark
    fun addOrReplaceExistingFirstElement(): PluginStorage<String> {
        storage.addOrReplace(
            predicate = { it == "init-0" || it == "replaced" },
            item = "replaced",
        )
        return storage
    }
}
