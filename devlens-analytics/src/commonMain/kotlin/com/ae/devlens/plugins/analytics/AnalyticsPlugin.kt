package com.ae.devlens.plugins.analytics

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.ae.devlens.core.PluginContext
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.plugins.analytics.store.AnalyticsStore
import com.ae.devlens.plugins.analytics.ui.AnalyticsContent
import com.ae.devlens.plugins.analytics.ui.AnalyticsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Plugin for tracking and inspecting analytics events inside the DevLens panel.
 *
 * ## Installation
 * ```kotlin
 * AEDevLens.default.install(AnalyticsPlugin())
 * ```
 *
 * ## Recording events:
 * ```kotlin
 * val analytics = AEDevLens.default.getPlugin<AnalyticsPlugin>()?.api
 * analytics?.track("button_tap", mapOf("screen" to "home"))
 * analytics?.screen("ProductDetail", mapOf("productId" to "123"))
 * ```
 */
public class AnalyticsPlugin : UIPlugin {
    override val id: String = ID
    override val name: String = "Analytics"
    override val icon: ImageVector = Icons.Default.Analytics

    private val _badgeCount = MutableStateFlow<Int?>(null)
    override val badgeCount: StateFlow<Int?> = _badgeCount

    private val store = AnalyticsStore()
    private lateinit var viewModel: AnalyticsViewModel

    /** Public API for recording events from your analytics adapters. */
    public val api: AnalyticsApi = AnalyticsApi(store)

    override fun onAttach(context: PluginContext) {
        viewModel = AnalyticsViewModel(store, context.scope)
        // Update badge count whenever events change
        CoroutineScope(context.scope.coroutineContext).launch {
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
        if (::viewModel.isInitialized) {
            AnalyticsContent(viewModel = viewModel, modifier = modifier)
        }
    }

    public companion object {
        public const val ID: String = "ae_devlens_analytics"
    }
}
