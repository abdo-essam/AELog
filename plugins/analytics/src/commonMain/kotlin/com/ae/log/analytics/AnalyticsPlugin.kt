package com.ae.log.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.log.analytics.storage.AnalyticsStorage
import com.ae.log.analytics.ui.AnalyticsContent
import com.ae.log.analytics.ui.AnalyticsViewModel
import com.ae.log.plugin.PluginContext
import com.ae.log.plugin.UIPlugin
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

    private val storage = AnalyticsStorage(capacity = maxEntries)

    @kotlin.concurrent.Volatile private var viewModel: AnalyticsViewModel? = null

    /** Public API for recording events from your analytics adapters. */
    public val tracker: AnalyticsTracker = AnalyticsTracker(storage)

    override fun onAttach(context: PluginContext) {
        viewModel = AnalyticsViewModel(storage, context.scope)

        // Update badge count whenever events change
        context.scope.launch {
            storage.events.collect { events ->
                _badgeCount.value = events.size
            }
        }
    }

    override fun onClear() {
        storage.clear()
    }

    override fun export(): String =
        storage.events.value.joinToString("\n") { event ->
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
