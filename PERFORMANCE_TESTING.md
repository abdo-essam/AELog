# AELog Performance Testing

This document is the authoritative reference for how AELog's performance is tested and validated. It covers all 5 strategies implemented in this repository.

---

## Hot Paths & Performance Targets

Every strategy focuses on the same critical paths. This table is your ground truth:

| Operation | Target (JVM) | Location |
|---|---|---|
| `RingBuffer.add()` | < 100ns | `RingBufferBenchmark` |
| `RingBuffer.toList()` (cap=500) | < 5µs | `RingBufferBenchmark` |
| `PluginStorage.add()` (lock + toList + flow) | < 20µs | `PluginStorageBenchmark` |
| `LogRecorder.log()` (explicit tag) | < 20µs | `LogPipelineBenchmark` |
| `LogRecorder.log()` + `callerTag()` | < 200µs | `LogPipelineBenchmark` |
| `AELog.isEnabled = false` fast-path | < 100ns | `LogPipelineBenchmark` |
| `AELog.getPlugin<T>()` lookup | < 1µs | `LogPipelineBenchmark` |
| `EventBus.publish()` (tryEmit) | < 5µs | `EventBusPerformanceTest` |
| `NetworkRecorder.logRequest()` | < 50µs | `NetworkPipelineBenchmark` |
| Full network request+response pair | < 100µs | `NetworkPipelineBenchmark` |
| `IdGenerator.next()` (UUID) | < 10µs | `NetworkPipelineBenchmark` |

---

## Strategy 1: JMH Microbenchmarks (`:benchmarks` module)

**What**: Statistically rigorous JMH benchmarks running on the JVM with proper warm-up and GC stabilisation.  
**When to run**: Before releases, after any structural changes to storage or the plugin pipeline.  
**Location**: [`benchmarks/src/jvmMain/kotlin/com/ae/log/benchmarks/`](benchmarks/src/jvmMain/kotlin/com/ae/log/benchmarks/)

### Benchmark classes

| File | What it measures |
|---|---|
| `RingBufferBenchmark` | Raw `add`, `toList`, and `addThenSnapshot` throughput across capacities 100/500/1000 |
| `PluginStorageBenchmark` | Full `add` chain (lock + ring + StateFlow), `updateFirst`, and `addOrReplace` |
| `LogPipelineBenchmark` | End-to-end log call — explicit tag, callerTag, disabled fast-path, plugin lookup, export |
| `NetworkPipelineBenchmark` | `logRequest`, full req/resp pair, body patching, UUID generation |

### Running the benchmarks

```bash
# Run ALL benchmarks (full mode: 5 warm-ups + 10 iterations × 1s each)
./gradlew :benchmarks:jvmBenchmark

# Run a specific class
./gradlew :benchmarks:jvmBenchmark --args ".*RingBuffer.*"
./gradlew :benchmarks:jvmBenchmark --args ".*LogPipeline.*"
./gradlew :benchmarks:jvmBenchmark --args ".*NetworkPipeline.*"
./gradlew :benchmarks:jvmBenchmark --args ".*PluginStorage.*"

# Quick smoke run (1 warm-up + 3 iterations × 500ms — fast feedback)
./gradlew :benchmarks:jvmBenchmark -Pbenchmark.config=smoke
```

Results are printed to stdout and saved under `benchmarks/build/reports/benchmarks/`.

---

## Strategy 2: Cross-Platform `commonTest` Stress Tests

**What**: `measureTime`-gated tests that run on ALL KMP targets as part of the normal test suite. Not statistically rigorous, but they catch obvious regressions across platforms (Android JVM, iOS Simulator).  
**When to run**: On every PR (`./gradlew allTests`).  
**Location**: Inside the existing `commonTest` source sets.

### Test classes

| Module | File | What it covers |
|---|---|---|
| `:core` | `StoragePerformanceTest` | RingBuffer 100k/1M adds, toList ×10k, PluginStorage 10k adds + updateFirst + addOrReplace |
| `:core` | `EventBusPerformanceTest` | EventBus 50k publishes, zero-drop burst, concurrent coroutine publishers |
| `:plugins:logs` | `LogRecorderPerformanceTest` | LogRecorder 5k entries, severity filter fast-path, ring eviction correctness, export |
| `:plugins:network` | `NetworkRecorderPerformanceTest` | 1k req/resp pairs, 500 pending requests, body patching, ring eviction, 10k UUID gen |

### Running the tests

```bash
# All targets, all modules
./gradlew allTests

# Only JVM (fastest)
./gradlew :core:jvmTest
./gradlew :plugins:logs:jvmTest
./gradlew :plugins:network:jvmTest

# iOS Simulator (on macOS)
./gradlew :core:iosSimulatorArm64Test
```

Each test prints a `[Perf]` line to stdout with the actual elapsed time, e.g.:
```
[Perf] RingBuffer.add ×100k: 12.3ms  (capacity=500)
[Perf] PluginStorage.add ×10k (cap=500): 187ms  [lock+toList+StateFlow]
```

---

## Strategy 3: Android Profiler / Systrace (Sample App)

**What**: Interactive stress-test screen in the sample app designed to be used while Android Studio Profiler is attached.  
**When to use**: Pre-release validation on real devices and emulators.  
**Location**: [`sample/composeApp/src/commonMain/kotlin/com/ae/log/sample/ui/features/perf/PerfScreen.kt`](sample/composeApp/src/commonMain/kotlin/com/ae/log/sample/ui/features/perf/PerfScreen.kt)

### How to profile

1. Run the sample app on a device or emulator (debug build).
2. Open **Android Studio → View → Tool Windows → Profiler**.
3. Click **"+"** → select the `com.ae.log.sample` process.
4. Go to the **CPU** tab → click **Record** (choose *Callstack Sample* or *Java/Kotlin Method Trace*).
5. Navigate to the **"Perf"** tab in the sample app.
6. Tap one of the stress buttons.
7. Click **Stop** → inspect the flame chart.

### Stress buttons available

| Button | What it exercises |
|---|---|
| Fire 1,000 logs (explicit tag) | Full log pipeline without stack capture |
| Fire 500 logs (auto callerTag) | `callerTag()` — Throwable creation + stack parse |
| Fire mixed severity burst (2,000 logs) | Mixed severities through the severity filter |
| Record 500 request/response pairs | Two-phase network recording pipeline |
| Record 200 pending requests | `logRequest` only — addOrReplace path |
| Overflow ring buffer (600 logs) | Ring eviction in steady-state |
| 10,000 calls while disabled | `isEnabled = false` atomic fast-path |

### What to look for in the flame chart

| Flame chart symbol | Interpretation |
|---|---|
| `RingBuffer.toList` is wide | O(n) copy is the dominant cost — consider lazy snapshotting |
| `callerTag` is wide | Stack trace capture is expensive — advise users to provide explicit tags |
| `MutableStateFlow.setValue` | StateFlow emission overhead — check if subscribers are backed up |
| `synchronized` (lock acquisition) | Lock contention under multi-thread write load |

---

## Strategy 4: Integration Throughput Tests

**What**: End-to-end tests that verify correctness AND performance together. Use real `LogStorage`/`NetworkStorage` (not mocks) so integration-level bugs are caught.  
**When to run**: On every PR alongside unit tests.  
**Location**: Same as Strategy 2 test files above (they're combined into the same test classes).

### Key integration scenarios

```
LogRecorder:
  5,000 log calls → ring buffer → StateFlow → buffer holds exactly 500 entries

NetworkRecorder:
  1,000 request + response pairs → correct pairing by ID → ring eviction at cap=200

Export:
  500 log entries → AELog.export() → non-empty string → < 100ms
```

---

## Strategy 5: Memory Profiling

**What**: Detecting allocation pressure and memory leaks.  
**When to use**: After capacity changes or after adding new features that touch the storage layer.

### Using Android Studio Memory Profiler

1. Run the sample app.
2. Open **Profiler → Memory** tab.
3. Click **"Record allocations"**.
4. Trigger the "Fire 1,000 logs" button on the Perf screen.
5. Stop recording → look for:
   - **`ArrayList` allocations** inside `RingBuffer.toList()` — one per log call.
   - **`StateFlow` emission objects** — should be minimal.
   - Any **`LogEntry`** or **`NetworkEntry`** objects not being collected.

### Key memory concerns

| Component | Concern | Impact |
|---|---|---|
| `PluginStorage.add()` | Calls `toList()` on every write → one `ArrayList` per log call | High allocation rate |
| `LogRecorder.log()` with throwable | `"$message\n${throwable.stackTraceToString()}"` → large String | Memory spike on error logs |
| `callerTag()` | Creates a `Throwable` per call → short-lived but adds GC pressure | Increases minor GC frequency |
| `NetworkEntry.copy()` | Kotlin data class copy on every response update | Predictable, low impact |

---

## Architecture Notes (Performance Context)

Understanding these design decisions helps interpret benchmark results:

### Why `PluginStorage.add()` calls `toList()` on every write

`PluginStorage` wraps `RingBuffer` and exposes a `StateFlow<List<T>>` for the UI. Because `StateFlow` requires a complete snapshot (not a delta), a full `toList()` copy is made on every `add`. This is an intentional tradeoff: reactive UI with zero subscriber coordination overhead, at the cost of O(n) allocation per write.

**Mitigation**: The ring buffer capacity is bounded (default 500 for logs, 200 for network), so the maximum copy is always O(capacity), not O(unbounded).

### Why `callerTag()` is expensive

`callerTag()` captures the caller's class name from the thread's stack trace. On JVM, this creates a `Throwable` and parses its string representation. On iOS, this calls into Kotlin/Native's stack unwinding. This is inherently slow (~20–100µs) and should be avoided in hot loops.

**Recommendation**: Always use `AELog.log.d("MyTag", "msg")` (explicit tag) in performance-sensitive code. Use the auto-tag overload (`AELog.log.d("msg")`) only in one-off or infrequent call sites.

### Why `EventBus.publish()` uses `tryEmit` (not `emit`)

`tryEmit` is non-suspending and lock-free. It can drop events if the buffer is full (capacity = 64), but AELog only publishes lifecycle events (panel open/close, app start/stop) — never per-log events. These are low-frequency by design, so 64 slots is more than sufficient and the non-blocking nature is critical for call-site safety.

---

## CI Integration

### What runs on every PR (`ci.yml`)

The `build-android` job already runs `./gradlew jvmTest` which includes all `*Performance*` tests.
After each run, a **"Extract Performance Timings"** step parses the XML test reports and posts a table directly into the **GitHub Actions Job Summary** — visible in the PR's Checks tab without downloading anything.

**What you see in the PR:**

```
⚡ Performance Test Results

| Test                                          | Timing     |
|-----------------------------------------------|------------|
| RingBuffer.add ×100k (capacity=500)           | 12.6ms     |
| PluginStorage.add ×10k [lock+toList+StateFlow]| 22.9ms     |
| EventBus.publish ×50k                         | 5.8ms      |
| LogRecorder.log ×5k                           | 25.7ms     |
| NetworkRecorder req+resp ×1000                | 35.9ms     |
| AELog.export (500 entries)                    | 4.6ms      |
```

If any test **fails its time budget** (e.g. `25ms > 2s limit`), the `Unit Tests` step fails and the PR is blocked. ✅

---

### JMH Benchmarks on demand (`benchmarks.yml`)

JMH benchmarks are **not** in the regular CI pipeline (they need a quiet, unloaded machine for stable numbers). Instead, trigger them manually:

**How to trigger from GitHub Actions tab:**

1. Go to **Actions → Benchmarks** in your repo
2. Click **"Run workflow"**
3. Fill in the inputs:
   - **Filter** (optional): e.g. `Storage`, `LogPipeline`, `Network` — or leave blank for all
   - **Compare ref** (optional): a branch, tag, or SHA to diff against

**Result — visible in the Job Summary:**

```
⚡ JMH Benchmark Results
Branch: `main`  |  Runner: macOS arm64  |  Filter: all

### Results
StorageBenchmark.addSingle             thrpt   10     312.4 ±  4.2  ops/us
LogPipelineBenchmark.logWithExplicitTag  avgt   10     8.21 ±  0.1  us/op
NetworkPipelineBenchmark.fullRequest    avgt   10    18.45 ±  0.3  us/op

### Diff vs `develop`
- StorageBenchmark.addSingle: 271.3 → 312.4   (-13.2%)   ← faster ✓
+ LogPipelineBenchmark.logWithExplicitTag: 7.1 → 8.2  (+15.4%)  ← slower ⚠
```

> Lines starting with `-` = value decreased · `+` = value increased  
> Whether that's good depends on the metric: `ops/us` higher is better, `ns/op`/`us/op` lower is better.

**Raw result files** are also uploaded as artifacts (90-day retention) so you can download them and run `./scripts/benchmark-diff.sh` locally.

**Auto-runs on release tags** (`v*`) so every published version has a benchmark baseline saved as an artifact.

