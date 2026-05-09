@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.core.PluginContext
import com.ae.log.core.UIPlugin
import com.ae.log.plugins.analytics.store.AnalyticsStore
import com.ae.log.plugins.analytics.ui.AnalyticsContent
import com.ae.log.plugins.analytics.ui.AnalyticsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Plugin for tracking and inspecting analytics events inside the AELog panel.
 *
 * ## Installation
 * ```kotlin
 * AELog.init(AnalyticsPlugin())
 * ```
 *
 * ## Recording events:
 * ```kotlin
 * val analytics = AELog.getPlugin<AnalyticsPlugin>()?.tracker
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

    private val _badgeCount = MutableStateFlow(0)
    override val badgeCount: StateFlow<Int> = _badgeCount

    private val store = AnalyticsStore(capacity = maxEntries)
    @Volatile private var viewModel: AnalyticsViewModel? = null

    /** Public API for recording events from your analytics adapters. */
    public val tracker: AnalyticsTracker = AnalyticsTracker(store)

    override fun onAttach(context: PluginContext) {
        viewModel = AnalyticsViewModel(store, context.scope)

        // Update badge count whenever events change
        context.scope.launch {
            store.events.collect { events ->
                _badgeCount.value = events.size
            }
        }
    }

    override fun onClear() {
        store.clear()
    }

    override fun export(): String =
        store.events.value.joinToString("\n") { event ->
            "Event: ${event.name} | Source: ${event.source?.sourceName} | Time: ${event.timestamp}\nProperties: ${event.properties}"
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
