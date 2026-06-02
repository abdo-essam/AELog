package com.ae.log.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ae.log.AELog
import com.ae.log.plugin.UIPlugin
import com.ae.log.ui.components.LogNotchButton
import com.ae.log.ui.layout.LogContainer
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogTheme

/**
 * A zero-wrap overlay container for AELog.
 *
 * Drop this as a sibling anywhere in your composition tree — no content wrapping required.
 * The notch trigger and the panel render via system-level [Popup]s, so they appear
 * above your content without needing to be a layout parent.
 *
 * ## Hiding the Notch / Custom Triggers
 * If you want to hide the floating top notch trigger (e.g. to avoid overlapping with
 * your app's top bar or notch), but still keep the library fully active and record
 * network requests/logs, set [showNotch] to `false`.
 *
 * You can then open the overlay panel programmatically from a custom debug gesture,
 * a developer-only settings button, or a shake trigger:
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     // Renders AELog but hides the top floating island notch trigger
 *     AELogOverlay(showNotch = false)
 *
 *     Scaffold {
 *         Button(onClick = { AELog.show() }) {
 *             Text("Open Inspector Panel")
 *         }
 *     }
 * }
 * ```
 *
 * @param showNotch Whether to display the default top-center floating notch trigger.
 */
@Composable
public fun AELogOverlay(showNotch: Boolean = AELog.config?.showNotch ?: true) {
    val isEnabled by AELog.isEnabledFlow.collectAsState()
    val instance = AELog.instance
    if (!isEnabled || instance == null) return

    val controller = remember { LogController(backing = instance.overlayVisible) }
    val isVisible by controller.isVisible.collectAsState()

    val plugins by instance.plugins.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isLargeScreen = maxWidth > LogDimens.largeScreenBreakpoint && maxHeight > 480.dp

        LogTheme {
            // Notch pill — Dynamic Island-style trigger at the top of the screen.
            // Hidden while the panel is open so it doesn't overlap.
            if (!isVisible && showNotch) {
                Popup(
                    alignment = Alignment.CenterEnd,
                    properties = PopupProperties(focusable = false),
                ) {
                    LogNotchButton(onClick = { controller.show() })
                }
            }

            // ModalBottomSheet / Dialog are already Popup-based — no layout parent needed.
            if (isVisible) {
                LogContainer(
                    plugins = uiPlugins,
                    isLargeScreen = isLargeScreen,
                    controller = controller,
                    onDismiss = { controller.hide() },
                )
            }
        }
    }
}
