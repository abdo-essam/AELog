package com.ae.log

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ae.log.core.AELogPlugin
import com.ae.log.core.LocalLogController
import com.ae.log.core.LogController
import com.ae.log.core.UIPlugin
import com.ae.log.ui.LogContainer
import com.ae.log.ui.LogFloatingButton
import com.ae.log.ui.theme.LogSpacing
import com.ae.log.ui.theme.LogTheme

/**
 * Top-level composable wrapper that enables the AELog overlay and initialises it.
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     AELogProvider(
 *         plugins = listOf(LogPlugin(), NetworkPlugin(), AnalyticsPlugin()),
 *         enabled = BuildConfig.DEBUG
 *     ) {
 *         MaterialTheme { MainNavigation() }
 *     }
 * }
 * ```
 */
@Composable
public fun AELogProvider(
    plugins: List<AELogPlugin>,
    uiConfig: AELogUiConfig = AELogUiConfig(),
    config: AELogConfig = AELogConfig(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (enabled) {
        AELog.init(*plugins.toTypedArray(), config = config)
    }

    AELogProvider(
        instance = if (enabled) AELog.defaultOrNull() else null,
        uiConfig = uiConfig,
        enabled = enabled,
        content = content,
    )
}

/**
 * Top-level composable wrapper that enables the AELog overlay.
 *
 * Wrap your entire app content with this composable.
 * If [instance] is null, this acts as a transparent pass-through.
 *
 * @param instance  The [AELog] instance to connect to. Defaults to [AELog.defaultOrNull].
 * @param uiConfig  UI-specific configuration (button visibility, theme, etc.).
 * @param enabled   Set to `false` in release builds for zero overhead.
 * @param content   Your app's content.
 */
@Composable
public fun AELogProvider(
    instance: AELog? = AELog.defaultOrNull(),
    uiConfig: AELogUiConfig = AELogUiConfig(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    if (!enabled || instance == null) {
        content()
        return
    }

    val inspector = instance

    val controller = remember { LogController() }
    val isVisible by controller.isVisible.collectAsState()

    // Plugins are expected to be static after init, but we still use collectAsState()
    // because StateFlow.value is NOT observable by Compose — derivedStateOf won't help here.
    val plugins by inspector.plugins.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }

    // Notify plugins of open/close lifecycle events
    LaunchedEffect(isVisible) {
        if (isVisible) inspector.lifecycle.notifyOpen() else inspector.lifecycle.notifyClose()
    }

    CompositionLocalProvider(LocalLogController provides controller) {
        LogTheme(colorScheme = uiConfig.colorScheme) {
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
                    LogFloatingButton(
                        onClick = { controller.show() },
                        modifier =
                            Modifier
                                .align(uiConfig.floatingButtonAlignment)
                                .padding(end = LogSpacing.x5, bottom = uiConfig.floatingButtonOffset),
                    )
                }

                if (isVisible) {
                    LogContainer(
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
public val AELog.uiPlugins: List<UIPlugin>
    get() = plugins.plugins.value.filterIsInstance<UIPlugin>()
