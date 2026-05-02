package com.ae.log.sample.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.analytics.AnalyticsApi
import com.ae.log.sample.SampleState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen() {
    // Access via SampleState — no reified inline needed
    val api: AnalyticsApi? = SampleState.analyticsApi

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Analytics") },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
        )

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Simulate analytics events as they would appear in production",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Navigation events ──────────────────────────────────────────
            item { SectionLabel("🗺️ Navigation") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventButton("screen_view: Home") {
                            api?.screen("HomeScreen", mapOf("source" to "bottom_nav"))
                        }
                        EventButton("screen_view: Product Detail") {
                            api?.screen(
                                "ProductDetailScreen",
                                mapOf(
                                    "product_id" to "prod_42",
                                    "category" to "electronics",
                                ),
                            )
                        }
                        EventButton("screen_view: Checkout") {
                            api?.screen("CheckoutScreen", mapOf("items_count" to "3"))
                        }
                    }
                }
            }

            // ── User actions ───────────────────────────────────────────────
            item { SectionLabel("👆 User Actions") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventButton("button_tap: Add to Cart") {
                            api?.track(
                                "button_tap",
                                mapOf(
                                    "id" to "add_to_cart",
                                    "screen" to "product_detail",
                                    "product_id" to "prod_42",
                                ),
                            )
                        }
                        EventButton("button_tap: Buy Now") {
                            api?.track("button_tap", mapOf("id" to "buy_now", "screen" to "product_detail"))
                        }
                        EventButton("swipe: Dismiss Recommendation") {
                            api?.track("swipe", mapOf("direction" to "left", "target" to "recommendation_card"))
                        }
                        EventButton("long_press: Save Item") {
                            api?.track("long_press", mapOf("target" to "product_card", "product_id" to "prod_42"))
                        }
                    }
                }
            }

            // ── E-commerce ─────────────────────────────────────────────────
            item { SectionLabel("🛒 E-Commerce") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventButton("add_to_cart") {
                            api?.track(
                                "add_to_cart",
                                mapOf(
                                    "product_id" to "prod_42",
                                    "name" to "Keyboard",
                                    "price" to "49.99",
                                    "currency" to "USD",
                                ),
                            )
                        }
                        EventButton("purchase (Firebase source)") {
                            api?.track(
                                "purchase",
                                mapOf(
                                    "transaction_id" to "txn_${System.currentTimeMillis()}",
                                    "value" to "149.99",
                                    "currency" to "USD",
                                    "items_count" to "3",
                                ),
                                source = com.ae.log.plugins.analytics.model.DefaultAdapterSource.FIREBASE,
                            )
                        }
                        EventButton("refund") {
                            api?.track(
                                "refund",
                                mapOf(
                                    "order_id" to "ord_123",
                                    "amount" to "49.99",
                                    "reason" to "not_as_described",
                                ),
                            )
                        }
                    }
                }
            }

            // ── Engagement ─────────────────────────────────────────────────
            item { SectionLabel("💡 Engagement") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        EventButton("search") {
                            api?.track("search", mapOf("query" to "kotlin multiplatform", "results" to "127"))
                        }
                        EventButton("share") {
                            api?.track(
                                "share",
                                mapOf(
                                    "content_type" to "product",
                                    "method" to "whatsapp",
                                    "product_id" to "prod_42",
                                ),
                            )
                        }
                        EventButton("notification_opened") {
                            api?.track("notification_opened", mapOf("campaign" to "summer_sale", "variant" to "A"))
                        }
                    }
                }
            }

            // ── Stress tests ───────────────────────────────────────────────
            item { SectionLabel("⚡ Stress Tests") }
            item {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { repeat(10) { i -> api?.track("batch_event", mapOf("index" to "$i")) } },
                                modifier = Modifier.weight(1f),
                            ) { Text("10 events") }
                            OutlinedButton(
                                onClick = { repeat(50) { i -> api?.track("batch_event", mapOf("index" to "$i")) } },
                                modifier = Modifier.weight(1f),
                            ) { Text("50 events") }
                        }
                    }
                }
            }

            // ── Utils ──────────────────────────────────────────────────────
            item {
                OutlinedButton(
                    onClick = { api?.clear() },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Clear all analytics events") }
            }
        }
    }
}

@Composable
private fun EventButton(
    label: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label)
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
