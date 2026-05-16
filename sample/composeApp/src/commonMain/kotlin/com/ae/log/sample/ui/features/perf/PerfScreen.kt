package com.ae.log.sample.ui.features.perf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.AELog
import com.ae.log.logs.log
import com.ae.log.logs.model.LogSeverity
import com.ae.log.network.NetworkPlugin
import com.ae.log.sample.ui.components.ActionButton
import com.ae.log.sample.ui.components.ActionCard
import com.ae.log.sample.ui.components.SectionHeader

/**
 * Strategy 3 (Android Profiler hook): A dedicated screen in the sample app
 * for stress-testing AELog performance interactively on a real device.
 *
 * ## How to use with Android Studio Profiler
 *
 * 1. Run the sample app on a device or emulator (debug build).
 * 2. Open Android Studio → View → Tool Windows → Profiler.
 * 3. Click the "+" button → select your process.
 * 4. Go to the "CPU" tab → click "Record" (choose "Callstack Sample" or "Java/Kotlin Method Trace").
 * 5. Tap a stress button here to trigger the workload.
 * 6. Click "Stop" → inspect the flame chart.
 *
 * Look for hot spots in:
 *   - `RingBuffer.toList()`       — O(n) copy inside synchronized block
 *   - `PluginStorage.add()`       — lock + toList + StateFlow emit
 *   - `callerTag()`               — Throwable creation + stack trace parsing
 *   - `MutableStateFlow.setValue` — equality check + subscriber notification
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Performance Stress Tests") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Log pipeline ──────────────────────────────────────────────

            item {
                SectionHeader("Log Pipeline")
                ActionCard(
                    title = "Log Throughput",
                    description = "Fires many log calls rapidly — attach the Profiler before tapping.",
                ) {
                    ActionButton("Fire 1,000 logs (explicit tag)", Color(0xFF4CAF50)) {
                        repeat(1_000) { i ->
                            AELog.log.d("PerfTest", "Log entry #$i — explicit tag path")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Fire 500 logs (auto callerTag)", Color(0xFFFF9800)) {
                        // WARNING: callerTag() captures a stack trace on every call.
                        // This should be visibly slower in the Profiler than the explicit-tag path.
                        repeat(500) { i ->
                            AELog.log.d("Auto-tag log #$i — watch callerTag() in Profiler")
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Fire mixed severity burst (2,000 logs)", Color(0xFF9C27B0)) {
                        val severities = LogSeverity.entries
                        repeat(2_000) { i ->
                            val sev = severities[i % severities.size]
                            AELog.log.d("PerfBurst", "[$sev] Entry #$i")
                        }
                    }
                }
            }

            // ── Network pipeline ──────────────────────────────────────────

            item {
                SectionHeader("Network Pipeline")
                ActionCard(
                    title = "Network Throughput",
                    description = "Simulates rapid request/response logging — no real HTTP calls.",
                ) {
                    ActionButton("Record 500 request/response pairs", Color(0xFF2196F3)) {
                        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@ActionButton
                        repeat(500) { i ->
                            val id = recorder.newId()
                            recorder.logRequest(
                                id = id,
                                url = "https://api.example.com/items/$i",
                                method = if (i % 2 == 0) "GET" else "POST",
                                headers = mapOf("Authorization" to "Bearer tok"),
                                body = if (i % 2 != 0) """{"index":$i}""" else null,
                            )
                            recorder.logResponse(
                                id = id,
                                statusCode = 200,
                                body = """{"id":$i,"ok":true}""",
                                durationMs = (10L + (i % 300)),
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Record 200 pending requests (no response)", Color(0xFFF44336)) {
                        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@ActionButton
                        repeat(200) { i ->
                            val id = recorder.newId()
                            recorder.logRequest(
                                id = id,
                                url = "https://slow.api.example.com/stream/$i",
                                method = "GET",
                            )
                        }
                    }
                }
            }

            // ── Storage eviction ──────────────────────────────────────────

            item {
                SectionHeader("Ring Buffer Eviction")
                ActionCard(
                    title = "Overflow Scenario",
                    description = "Exceeds log buffer capacity (default 500) to trigger ring eviction.",
                ) {
                    ActionButton("Overflow ring buffer (600 logs)", MaterialTheme.colorScheme.error) {
                        repeat(600) { i ->
                            AELog.log.d("Overflow", "Entry #$i — should evict oldest")
                        }
                    }
                }
            }

            // ── isEnabled fast-path ───────────────────────────────────────

            item {
                SectionHeader("Fast-Path Guards")
                ActionCard(
                    title = "isEnabled = false Fast Exit",
                    description = "Verifies that disabled AELog adds near-zero overhead to the call site.",
                ) {
                    ActionButton("10,000 calls while disabled", Color(0xFF607D8B)) {
                        val was = AELog.isEnabled
                        AELog.isEnabled = false
                        repeat(10_000) {
                            AELog.log.d("ShouldBeSkipped", "This never reaches storage")
                        }
                        AELog.isEnabled = was
                    }
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
