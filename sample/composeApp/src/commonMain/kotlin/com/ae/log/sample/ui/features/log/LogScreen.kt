package com.ae.log.sample.ui.features.log

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.AELog
import com.ae.log.log
import com.ae.log.sample.ui.components.ActionButton
import com.ae.log.sample.ui.components.ActionCard
import com.ae.log.sample.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Standard Logs") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader("Basic Logging")
                ActionCard(
                    title = "Standard Severity Levels",
                    description = "AELog.log.d(), .i(), .e(), etc.",
                ) {
                    ActionButton("Log VERBOSE (Gray)", Color(0xFF9E9E9E)) {
                        AELog.log.v("Simple logs are easy")
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Log DEBUG (Green)", Color(0xFF4CAF50)) {
                        AELog.log.d("API", "User data fetched successfully")
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Log ERROR (Red)", Color(0xFFF44336)) {
                        AELog.log.e("Database", "Failed to write record", RuntimeException("Disk Full"))
                    }
                }
            }

            item {
                SectionHeader("Stress Testing")
                ActionCard(
                    title = "Performance Test",
                    description = "Sending multiple logs to verify the ring buffer",
                ) {
                    ActionButton("Send 20 logs", MaterialTheme.colorScheme.secondary) {
                        repeat(20) { i -> AELog.log.d("Batch", "Entry #${i + 1}") }
                    }
                }
            }
        }
    }
}
