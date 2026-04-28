package com.ae.logs.sample.ui.network

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.logs.plugins.network.NetworkApi
import com.ae.logs.plugins.network.model.NetworkMethod
import com.ae.logs.sample.SampleState
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScreen() {
    // Access via SampleState — no reified inline needed
    val api: NetworkApi? = SampleState.networkApi
    val client = SampleState.httpClient
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Network") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // ── Live Calls — real HTTP via AELogsKtorPlugin ────────────────
            item { SectionLabel("🌐 Live HTTP Calls (real network)") }
            item {
                Text(
                    "Tap a button — the request hits jsonplaceholder.typicode.com " +
                        "and appears in the Network inspector automatically.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        client?.get("https://jsonplaceholder.typicode.com/posts/1")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MethodBadge("GET", Color(0xFF2196F3))
                                Text("/posts/1")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        client?.get("https://jsonplaceholder.typicode.com/users")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MethodBadge("GET", Color(0xFF2196F3))
                                Text("/users")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    runCatching {
                                        client?.post("https://jsonplaceholder.typicode.com/posts") {
                                            headers {
                                                append("Content-Type", "application/json")
                                            }
                                            setBody(
                                                """{"title":"AELogs Test","body":"Real POST captured!","userId":1}""",
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MethodBadge("POST", Color(0xFF4CAF50))
                                Text("/posts")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    // Intentional bad URL — captured as a network error
                                    runCatching {
                                        client?.get("https://jsonplaceholder.typicode.com/posts/99999999")
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MethodBadge("GET", Color(0xFF2196F3))
                                Text("/posts/99999999  (→ 404)")
                            }
                        }
                    }
                }
            }

            // ── Simulated calls — manual NetworkApi ───────────────────────
            item { SectionLabel("🧪 Simulated Calls (manual NetworkApi)") }
            item {
                Text(
                    "Simulate intercepted HTTP requests",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }


            // ── Success responses ──────────────────────────────────────────
            item { SectionLabel("✅ Success (2xx)") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NetworkButton("GET  /posts/1", NetworkMethod.GET, 200) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://jsonplaceholder.typicode.com/posts/1", NetworkMethod.GET)
                            api.response(
                                id,
                                200,
                                """{"id":1,"userId":1,"title":"Post Title","body":"..."}""",
                                durationMs = 142,
                            )
                        }
                        NetworkButton("POST  /users", NetworkMethod.POST, 201) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(
                                id,
                                "https://api.example.com/users",
                                NetworkMethod.POST,
                                body = """{"name":"Ahmed","role":"developer"}""",
                            )
                            api.response(id, 201, """{"id":99,"name":"Ahmed"}""", durationMs = 289)
                        }
                        NetworkButton("PUT  /users/99", NetworkMethod.PUT, 200) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(
                                id,
                                "https://api.example.com/users/99",
                                NetworkMethod.PUT,
                                body = """{"name":"Ahmed Essam"}""",
                            )
                            api.response(id, 200, durationMs = 198)
                        }
                        NetworkButton("DELETE  /items/42", NetworkMethod.DELETE, 204) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://api.example.com/items/42", NetworkMethod.DELETE)
                            api.response(id, 204, durationMs = 67)
                        }
                    }
                }
            }

            // ── Client errors ──────────────────────────────────────────────
            item { SectionLabel("⚠️ Client Errors (4xx)") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NetworkButton("GET  /missing", NetworkMethod.GET, 404) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://api.example.com/missing/resource", NetworkMethod.GET)
                            api.response(id, 404, """{"error":"Not Found"}""", durationMs = 88)
                        }
                        NetworkButton("POST  /auth  (no token)", NetworkMethod.POST, 401) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://api.example.com/protected", NetworkMethod.POST)
                            api.response(id, 401, """{"error":"Unauthorized"}""", durationMs = 55)
                        }
                        NetworkButton("DELETE  /admin  (forbidden)", NetworkMethod.DELETE, 403) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://api.example.com/admin/users", NetworkMethod.DELETE)
                            api.response(id, 403, """{"error":"Forbidden"}""", durationMs = 72)
                        }
                    }
                }
            }

            // ── Server errors ──────────────────────────────────────────────
            item { SectionLabel("🔴 Server Errors (5xx)") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        NetworkButton("GET  /crash", NetworkMethod.GET, 500) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://api.example.com/crash", NetworkMethod.GET)
                            api.response(id, 500, "Internal Server Error", durationMs = 1240)
                        }
                        NetworkButton("POST  /service  (unavailable)", NetworkMethod.POST, 503) {
                            val id = api?.newId() ?: return@NetworkButton
                            api.request(id, "https://api.example.com/service", NetworkMethod.POST)
                            api.response(id, 503, durationMs = 30044)
                        }
                    }
                }
            }

            // ── Network failures ───────────────────────────────────────────
            item { SectionLabel("❌ Network Failures") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                val id = api?.newId() ?: return@OutlinedButton
                                api.request(id, "https://unreachable.example.com/data", NetworkMethod.GET)
                                api.error(id, "java.net.SocketTimeoutException: timeout after 30000ms")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("Timeout error") }

                        OutlinedButton(
                            onClick = {
                                val id = api?.newId() ?: return@OutlinedButton
                                api.request(id, "https://no-dns.invalid/path", NetworkMethod.GET)
                                api.error(id, "java.net.UnknownHostException: Unable to resolve host")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text("DNS resolution failure") }
                    }
                }
            }

            // ── Utils ──────────────────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = { api?.clear() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Clear all network entries") }
            }
        }
    }
}

@Composable
private fun NetworkButton(
    label: String,
    method: NetworkMethod,
    statusCode: Int,
    onClick: () -> Unit,
) {
    val statusColor =
        when {
            statusCode in 200..299 -> Color(0xFF4CAF50)
            statusCode in 400..499 -> Color(0xFFFFC107)
            else -> Color(0xFFF44336)
        }
    val methodColor =
        when (method) {
            NetworkMethod.GET -> Color(0xFF2196F3)
            NetworkMethod.POST -> Color(0xFF4CAF50)
            NetworkMethod.PUT -> Color(0xFFFF9800)
            NetworkMethod.PATCH -> Color(0xFF9C27B0)
            NetworkMethod.DELETE -> Color(0xFFF44336)
            else -> Color(0xFF607D8B)
        }
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Method badge
            Box(
                modifier =
                    Modifier
                        .background(methodColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(method.label, style = MaterialTheme.typography.labelSmall, color = Color.White)
            }
            // URL
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
            )
            // Status badge
            Box(
                modifier =
                    Modifier
                        .size(32.dp)
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = statusCode.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                )
            }
        }
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

@Composable
private fun MethodBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color, RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
