package com.ae.devlens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ae.devlens.core.DevLensController
import com.ae.devlens.core.LocalDevLensController
import com.ae.devlens.core.UIPlugin
import com.ae.devlens.ui.AEDevLensContainer
import com.ae.devlens.ui.AEDevLensFloatingButton
import com.ae.devlens.ui.theme.AEDevLensTheme
import com.ae.devlens.ui.theme.DevLensSpacing

/**
 * Top-level composable wrapper that enables the DevLens overlay.
 *
 * Wrap your entire app content with this composable.
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     AEDevLensProvider(
 *         inspector = AEDevLens.default,
 *         enabled = BuildConfig.DEBUG,
 *     ) {
 *         MaterialTheme { MainNavigation() }
 *     }
 * }
 * ```
 *
 * @param inspector     The [AEDevLens] instance to observe.
 * @param uiConfig      UI-specific configuration (button visibility, theme, etc.).
 * @param enabled       Set to `false` in release builds for zero overhead.
 * @param content       Your app's content.
 */
@Composable
public fun AEDevLensProvider(
    inspector: AEDevLens = AEDevLens.default,
    uiConfig: DevLensUiConfig = DevLensUiConfig(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled) {
        content()
        return
    }

    val controller = remember { DevLensController() }
    val isVisible by controller.isVisible.collectAsState()

    // Plugins are expected to be static after init, but we still use collectAsState()
    // because StateFlow.value is NOT observable by Compose — derivedStateOf won't help here.
    val plugins by inspector.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }

    // Notify plugins of open/close lifecycle events
    LaunchedEffect(isVisible) {
        if (isVisible) inspector.notifyOpen() else inspector.notifyClose()
    }

    CompositionLocalProvider(LocalDevLensController provides controller) {
        AEDevLensTheme(colorScheme = uiConfig.colorScheme) {
            BoxWithConstraints(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .then(
                            if (uiConfig.enableLongPress) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures(onLongPress = { controller.show() })
                                }
                            } else {
                                Modifier
                            },
                        ),
            ) {
                content()

                if (uiConfig.showFloatingButton) {
                    AEDevLensFloatingButton(
                        onClick = { controller.show() },
                        modifier =
                            Modifier
                                .align(uiConfig.floatingButtonAlignment)
                                .padding(end = DevLensSpacing.x5, bottom = uiConfig.floatingButtonOffset),
                    )
                }

                if (isVisible) {
                    AEDevLensContainer(
                        plugins = uiPlugins,
                        isLargeScreen = maxWidth > 600.dp,
                        presentationMode = uiConfig.presentationMode,
                        onDismiss = { controller.hide() },
                    )
                }
            }
        }
    }
}

/**
 * Extension to get all [UIPlugin]s from this inspector.
 * Available in devlens-ui so devlens-core stays Compose-free.
 */
public val AEDevLens.uiPlugins: List<UIPlugin>
    get() = plugins.value.filterIsInstance<UIPlugin>()
