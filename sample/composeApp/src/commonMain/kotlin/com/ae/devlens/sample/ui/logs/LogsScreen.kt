package com.ae.devlens.sample.ui.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.devlens.DevLens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Logs") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Quick actions ───────────────────────────────────────────────
            item {
                SectionLabel("Quick Log")
            }
            item {
                QuickLogRow()
            }

            // ── All levels ──────────────────────────────────────────────────
            item { SectionLabel("All Severity Levels") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LogLevelButton(
                            "VERBOSE",
                            Color(0xFF9E9E9E),
                        ) { DevLens.v("Sample", "Verbose — lowest priority, for granular tracing") }
                        LogLevelButton(
                            "DEBUG",
                            Color(0xFF4CAF50),
                        ) { DevLens.d("Sample", "Debug — useful during development") }
                        LogLevelButton(
                            "INFO",
                            Color(0xFF2196F3),
                        ) { DevLens.i("Sample", "Info — general informational message") }
                        LogLevelButton(
                            "WARN",
                            Color(0xFFFFC107),
                        ) { DevLens.w("Sample", "Warn — something unexpected but recoverable") }
                        LogLevelButton("ERROR", Color(0xFFF44336)) {
                            DevLens.e("Sample", "Error — something failed!", RuntimeException("Sample error"))
                        }
                        LogLevelButton(
                            "ASSERT",
                            Color(0xFF9C27B0),
                        ) { DevLens.wtf("Sample", "WTF — this should never happen!") }
                    }
                }
            }

            // ── Stress tests ────────────────────────────────────────────────
            item { SectionLabel("Stress Tests") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick = {
                                repeat(
                                    20,
                                ) { i -> DevLens.d("Batch", "Entry #${i + 1} — stress test 20 logs") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Send 20 logs") }

                        OutlinedButton(
                            onClick = {
                                repeat(
                                    100,
                                ) { i -> DevLens.d("BigBatch", "Entry #${i + 1} — stress test 100 logs") }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Send 100 logs (ring buffer test)") }

                        OutlinedButton(
                            onClick = {
                                DevLens.d("Multi", "Step 1: starting operation")
                                DevLens.i("Multi", "Step 2: fetching data")
                                DevLens.w("Multi", "Step 3: slow response detected")
                                DevLens.e("Multi", "Step 4: fallback triggered")
                                DevLens.i("Multi", "Step 5: recovered successfully")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Simulate 5-step operation") }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLogRow() {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            QuickIconButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.BugReport, null, tint = Color(0xFF4CAF50)) },
                label = "Debug",
            ) { DevLens.d("Quick", "Quick debug") }

            QuickIconButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Info, null, tint = Color(0xFF2196F3)) },
                label = "Info",
            ) { DevLens.i("Quick", "Quick info") }

            QuickIconButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Warning, null, tint = Color(0xFFFFC107)) },
                label = "Warn",
            ) { DevLens.w("Quick", "Quick warn") }

            QuickIconButton(
                modifier = Modifier.weight(1f),
                icon = { Icon(Icons.Default.Error, null, tint = Color(0xFFF44336)) },
                label = "Error",
            ) { DevLens.e("Quick", "Quick error") }
        }
    }
}

@Composable
private fun QuickIconButton(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 4.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            icon()
            Text(text = label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LogLevelButton(
    label: String,
    color: Color,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = color),
    ) {
        Text(label, color = Color.White)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}
