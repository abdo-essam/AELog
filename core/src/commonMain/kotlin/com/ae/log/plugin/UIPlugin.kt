package com.ae.log.plugin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A plugin that provides a visible tab/panel in the AELog UI.
 *
 * Implement this interface to add a visual debugging panel (e.g., logs, network, storage).
 * For headless plugins that only collect data without a UI, implement [Plugin] directly.
 *
 * ### Minimal implementation — every property has a sensible default:
 * ```kotlin
 * class MyPlugin : UIPlugin {
 *     override val name = "My Panel"
 *
 *     @Composable
 *     override fun Content(modifier: Modifier) {
 *         // your UI here
 *     }
 * }
 * ```
 *
 * ### Icon
 * The [icon] slot accepts any composable; you are not restricted to [androidx.compose.ui.graphics.vector.ImageVector]:
 * ```kotlin
 * override val icon: @Composable () -> Unit = { Icon(Icons.Default.List, contentDescription = null) }
 * override val icon: @Composable () -> Unit = { Image(painterResource(Res.drawable.my_icon), null) }
 * override val icon: @Composable () -> Unit = { MyAnimatedIcon() }
 * ```
 * If omitted, a generic plug/extension icon is shown automatically.
 *
 * ### Badge
 * Override [badgeCount] only when you need a live counter on the tab.
 * Omit it entirely when you don't — the default shows no badge.
 * ```kotlin
 * private val _count = MutableStateFlow(0)
 * override val badgeCount: StateFlow<Int> = _count
 * ```
 */
public interface UIPlugin : Plugin {
    /**
     * Composable rendered as the tab icon in the AELog panel.
     *
     * Accepts any composable, so bitmaps, painters, and custom animations are all valid.
     * Defaults to a generic [Icons.Default.Extension] placeholder — override only when
     * a more specific icon is meaningful.
     */
    public val icon: @Composable () -> Unit
        get() = { Icon(Icons.Default.Extension, contentDescription = null) }

    /**
     * Live count shown as a badge on the plugin's tab.
     * A value <= 0 hides the badge entirely.
     *
     * **Defaults to [NoBadge]** — an immutable flow that always emits 0.
     * Override only when your plugin actively tracks a meaningful count (e.g., number of
     * captured logs, pending crash reports, etc.).
     */
    public val badgeCount: StateFlow<Int>
        get() = NoBadge

    /** The plugin's main UI content, rendered inside the AELog panel body. */
    @Composable
    public fun Content(modifier: Modifier)

    public companion object {
        /**
         * Shared, immutable "no badge" sentinel.
         * Returned by the default [badgeCount] implementation so that a single object
         * is reused across all plugins that don't need a badge counter.
         */
        public val NoBadge: StateFlow<Int> = MutableStateFlow(0)
    }
}
