package com.ae.logs

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ae.logs.core.AELogsController
import com.ae.logs.core.AELogsPlugin
import com.ae.logs.core.LocalAELogsController
import com.ae.logs.core.UIPlugin
import com.ae.logs.ui.AELogsContainer
import com.ae.logs.ui.AELogsFloatingButton
import com.ae.logs.ui.theme.AELogsSpacing
import com.ae.logs.ui.theme.AELogsTheme

/**
 * Top-level composable wrapper that enables the AELogs overlay and initialises it.
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     AELogsProvider(
 *         plugins = listOf(LogsPlugin(), NetworkPlugin(), AnalyticsPlugin()),
 *         enabled = BuildConfig.DEBUG
 *     ) {
 *         MaterialTheme { MainNavigation() }
 *     }
 * }
 * ```
 */
@Composable
public fun AELogsProvider(
    plugins: List<AELogsPlugin>,
    uiConfig: AELogsUiConfig = AELogsUiConfig(),
    config: AELogsConfig = AELogsConfig(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (enabled) {
        AELogs.init(*plugins.toTypedArray(), config = config)
    }

    AELogsProvider(
        instance = if (enabled) AELogs.defaultOrNull() else null,
        uiConfig = uiConfig,
        enabled = enabled,
        content = content,
    )
}

/**
 * Top-level composable wrapper that enables the AELogs overlay.
 *
 * Wrap your entire app content with this composable.
 * If [instance] is null, this acts as a transparent pass-through.
 *
 * @param instance  The [AELogs] instance to connect to. Defaults to [AELogs.defaultOrNull].
 * @param uiConfig  UI-specific configuration (button visibility, theme, etc.).
 * @param enabled   Set to `false` in release builds for zero overhead.
 * @param content   Your app's content.
 */
@Composable
public fun AELogsProvider(
    instance: AELogs? = AELogs.defaultOrNull(),
    uiConfig: AELogsUiConfig = AELogsUiConfig(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled || instance == null) {
        content()
        return
    }

    val inspector = instance

    val controller = remember { AELogsController() }
    val isVisible by controller.isVisible.collectAsState()

    // Plugins are expected to be static after init, but we still use collectAsState()
    // because StateFlow.value is NOT observable by Compose — derivedStateOf won't help here.
    val plugins by inspector.plugins.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }

    // Notify plugins of open/close lifecycle events
    LaunchedEffect(isVisible) {
        if (isVisible) inspector.lifecycle.notifyOpen() else inspector.lifecycle.notifyClose()
    }

    CompositionLocalProvider(LocalAELogsController provides controller) {
        AELogsTheme(colorScheme = uiConfig.colorScheme) {
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
                    AELogsFloatingButton(
                        onClick = { controller.show() },
                        modifier =
                            Modifier
                                .align(uiConfig.floatingButtonAlignment)
                                .padding(end = AELogsSpacing.x5, bottom = uiConfig.floatingButtonOffset),
                    )
                }

                if (isVisible) {
                    AELogsContainer(
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
 * Available in logs-ui so logs-core stays Compose-free.
 */
public val AELogs.uiPlugins: List<UIPlugin>
    get() = plugins.plugins.value.filterIsInstance<UIPlugin>()
