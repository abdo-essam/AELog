package com.ae.log.sample.ui.features.crashes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.AELog
import com.ae.log.crashes.crashes
import com.ae.log.sample.ui.components.ActionButton
import com.ae.log.sample.ui.components.ActionCard
import com.ae.log.sample.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrashScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Crash Reporting") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ── Non-Fatal Exceptions ──────────────────────────────────
            item {
                SectionHeader("Non-Fatal Exceptions")
                ActionCard(
                    title = "Recorded Non-Fatal",
                    description =
                        "AELog.crashes.recordNonFatal(e) — captured and persisted, " +
                            "app keeps running. Check the AELog overlay → Crashes tab.",
                ) {
                    ActionButton("Record NullPointerException", Color(0xFFFB8C00)) {
                        AELog.crashes.recordNonFatal(
                            NullPointerException("User profile reference was null"),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Record IllegalStateException", Color(0xFFFB8C00)) {
                        AELog.crashes.recordNonFatal(
                            IllegalStateException("Payment session expired"),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Record custom exception + thread", Color(0xFFFB8C00)) {
                        AELog.crashes.recordNonFatal(
                            RuntimeException("Cache invalidation failed after 3 retries"),
                            threadName = "cache-worker",
                        )
                    }
                }
            }

            // ── Chained Exceptions ────────────────────────────────────
            item {
                SectionHeader("Chained Exceptions")
                ActionCard(
                    title = "Exception with Cause",
                    description = "Records the full chain — root cause is visible in the stack trace.",
                ) {
                    ActionButton("Record chained IOException", Color(0xFFE91E63)) {
                        val root = Exception("SSL handshake failed")
                        AELog.crashes.recordNonFatal(
                            java.io.IOException("Network request failed", root),
                        )
                    }
                }
            }

            // ── Batch / Stress ────────────────────────────────────────
            item {
                SectionHeader("Stress Testing")
                ActionCard(
                    title = "Batch Non-Fatals",
                    description = "Records 10 non-fatal events back-to-back to verify storage capacity.",
                ) {
                    ActionButton("Record 10 non-fatals", MaterialTheme.colorScheme.secondary) {
                        repeat(10) { i ->
                            AELog.crashes.recordNonFatal(
                                RuntimeException("Batch error #${i + 1}"),
                                threadName = "batch-thread-$i",
                            )
                        }
                    }
                }
            }

            // ── Fatal ─────────────────────────────────────────────────
            item {
                SectionHeader("Fatal Crash (⚠ Terminates App)")
                ActionCard(
                    title = "Trigger Fatal Crash",
                    description =
                        "Throws an uncaught exception. AELog captures it before the " +
                            "process dies. Reopen the app and check the Crashes tab.",
                ) {
                    ActionButton(
                        label = "CRASH the app now",
                        color = Color(0xFFE53935),
                    ) {
                        throw RuntimeException("AELog fatal crash demo — intentional crash")
                    }
                }
            }
        }
    }
}
