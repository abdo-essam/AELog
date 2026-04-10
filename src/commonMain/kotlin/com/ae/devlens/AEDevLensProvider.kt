package com.ae.devlens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ae.devlens.core.AEDevLensController
import com.ae.devlens.core.LocalAEDevLensController
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.plugins.logs.LogsPlugin
import com.ae.devlens.ui.AEDevLensContainer
import com.ae.devlens.ui.AEDevLensFloatingButton
import com.ae.devlens.ui.theme.AEDevLensTheme
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Top-level composable wrapper that provides AEDevLens functionality.
 *
 * Wrap your entire app content with this to enable DevLens overlay.
 *
 * ## Usage
 * ```kotlin
 * @Composable
 * fun App() {
 *     AEDevLensProvider(
 *         enabled = BuildConfig.DEBUG
 *     ) {
 *         MaterialTheme {
 *             MainNavigation()
 *         }
 *     }
 * }
 * ```
 *
 * @param inspector The inspector instance to use. Defaults to [AEDevLens.default].
 * @param enabled Whether DevLens is enabled. Set to `false` in release builds for zero overhead.
 * @param content Your app content.
 */
@Composable
fun AEDevLensProvider(
    inspector: AEDevLens = AEDevLens.default,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    val controller = remember { AEDevLensController() }
    val isVisible by controller.isVisible.collectAsState()
    val config = inspector.config
    val plugins by inspector.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }

    // Set close callback on LogsPlugin
    LaunchedEffect(Unit) {
        inspector.getPlugin<LogsPlugin>()?.setOnCloseCallback {
            controller.hide()
        }
    }

    // Notify plugins of open/close lifecycle
    LaunchedEffect(isVisible) {
        if (isVisible) {
            inspector.notifyOpen()
        } else {
            inspector.notifyClose()
        }
    }

    CompositionLocalProvider(LocalAEDevLensController provides controller) {
        AEDevLensTheme(colorScheme = config.colorScheme) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .then(
                        if (config.longPressToOpenMs > 0) {
                            Modifier.pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { controller.show() }
                                )
                            }
                        } else {
                            Modifier
                        }
                    )
            ) {
                content()

                // Floating debug button
                if (config.showFloatingButton) {
                    AEDevLensFloatingButton(
                        onClick = { controller.show() },
                        modifier = Modifier
                            .align(config.floatingButtonAlignment)
                            .padding(
                                end = DevLensSpacing.x5,
                                bottom = 100.dp
                            )
                    )
                }

                // Inspector UI overlay
                if (isVisible) {
                    AEDevLensContainer(
                        plugins = uiPlugins,
                        isLargeScreen = false, // Default to phone layout
                        onDismiss = { controller.hide() }
                    )
                }
            }
        }
    }
}
