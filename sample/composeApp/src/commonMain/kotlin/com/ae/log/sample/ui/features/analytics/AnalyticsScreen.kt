package com.ae.log.sample.ui.features.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.AELog
import com.ae.log.plugins.analytics.analytics
import com.ae.log.sample.ui.components.ActionButton
import com.ae.log.sample.ui.components.ActionCard
import com.ae.log.sample.ui.components.SectionHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Event Tracking") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader("User Engagement")
                ActionCard(
                    title = "Events & Screens",
                    description = "Using AELog.analytics.logEvent()",
                ) {
                    ActionButton("Track 'purchase' event", Color(0xFF9C27B0)) {
                        AELog.analytics.logEvent(
                            name = "purchase_success",
                            properties = mapOf("amount" to "49.99", "currency" to "USD"),
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Log 'Home' Screen", Color(0xFF673AB7)) {
                        AELog.analytics.logScreen("HomeScreen")
                    }
                }
            }

            item {
                SectionHeader("Management")
                ActionButton("Clear Analytics", MaterialTheme.colorScheme.error) {
                    AELog.analytics.clear()
                }
            }
        }
    }
}
