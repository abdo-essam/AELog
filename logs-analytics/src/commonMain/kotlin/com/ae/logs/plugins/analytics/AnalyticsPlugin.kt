package com.ae.logs.plugins.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.logs.core.PluginContext
import com.ae.logs.core.UIPlugin
import com.ae.logs.plugins.analytics.store.AnalyticsStore
import com.ae.logs.plugins.analytics.ui.AnalyticsContent
import com.ae.logs.plugins.analytics.ui.AnalyticsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Plugin for tracking and inspecting analytics events inside the AELogs panel.
 *
 * ## Installation
 * ```kotlin
 * AELogs.default.install(AnalyticsPlugin())
 * ```
 *
 * ## Recording events:
 * ```kotlin
 * val analytics = AELogs.default.getPlugin<AnalyticsPlugin>()?.api
 * analytics?.track("button_tap", mapOf("screen" to "home"))
 * analytics?.screen("ProductDetail", mapOf("productId" to "123"))
 * ```
 */
public class AnalyticsPlugin(
    maxEntries: Int = 500,
) : UIPlugin {
    override val id: String = ID
    override val name: String = "Analytics"
    override val icon: ImageVector = Icons.Default.Analytics

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    private val store = AnalyticsStore(capacity = maxEntries)
    private var viewModel: AnalyticsViewModel? = null

    /** Public API for recording events from your analytics adapters. */
    public val api: AnalyticsApi = AnalyticsApi(store)

    override fun onAttach(context: PluginContext) {
        viewModel = AnalyticsViewModel(store, context.scope)

        // Register tag with logs plugin dynamically
        context.scope.launch {
            context.eventBus.publish(
                com.ae.logs.core.bus
                    .RegisterLogTagEvent("Analytics", "Analytics"),
            )
        }

        // Update badge count whenever events change
        context.scope.launch {
            store.events.collect { events ->
                _badgeCount.value = events.size.takeIf { it > 0 }
            }
        }
    }

    override fun onClear() {
        store.clear()
        _badgeCount.value = null
    }

    @Composable
    override fun Content(modifier: Modifier) {
        val vm = viewModel ?: return
        AnalyticsContent(viewModel = vm, modifier = modifier)
    }

    public companion object {
        public const val ID: String = "ae_logs_analytics"
    }
}

/** Type-safe accessor for the [AnalyticsApi] on the default [com.ae.logs.AELogs] instance. */
public val com.ae.logs.AELogs.Companion.analytics: AnalyticsApi?
    get() = plugin<AnalyticsPlugin>()?.api
