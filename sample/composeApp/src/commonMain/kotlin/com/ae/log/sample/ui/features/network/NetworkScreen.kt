package com.ae.log.sample.ui.features.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.AELog
import com.ae.log.network.network
import com.ae.log.sample.SampleState
import com.ae.log.sample.ui.components.ActionButton
import com.ae.log.sample.ui.components.ActionCard
import com.ae.log.sample.ui.components.SectionHeader
import io.ktor.client.request.get
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen() {
    val client = SampleState.httpClient
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Network Traffic") })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                SectionHeader("Live Interception")
                ActionCard(
                    title = "Real HTTP Requests",
                    description = "Captured automatically via AELogKtorInterceptor",
                ) {
                    ActionButton("GET /posts/1 (Ktor)", Color(0xFF2196F3)) {
                        scope.launch {
                            runCatching { client?.get("https://jsonplaceholder.typicode.com/posts/1") }
                        }
                    }
                }
            }

            item {
                SectionHeader("Manual Logging")
                ActionCard(
                    title = "Custom Entries",
                    description = "Using AELog.network.logRequest()",
                ) {
                    ActionButton("Log 200 OK", Color(0xFF4CAF50)) {
                        AELog.network.log(
                            method = "GET",
                            url = "https://api.example.com/status",
                            statusCode = 200,
                            responseBody = "{\"status\": \"healthy\"}",
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Log 404 Not Found", Color(0xFFFFC107)) {
                        AELog.network.log(
                            method = "POST",
                            url = "https://api.example.com/v1/auth",
                            statusCode = 404,
                            responseBody = "{\"error\": \"User not found\"}",
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    ActionButton("Log 500 Server Error", Color(0xFFF44336)) {
                        AELog.network.log(
                            method = "GET",
                            url = "https://api.example.com/crash",
                            statusCode = 500,
                            responseBody = "Internal Server Error",
                        )
                    }
                }
            }
        }
    }
}
