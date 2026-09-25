package com.ae.log.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.ae.log.AELog
import com.ae.log.plugin.UIPlugin
import com.ae.log.ui.components.LogNotchButton
import com.ae.log.ui.layout.LogContainer
import com.ae.log.ui.theme.LogDimens
import com.ae.log.ui.theme.LogTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private data class OverlayNotchDimensions(
    val maxX: Float,
    val maxY: Float,
    val defaultX: Float,
    val defaultY: Float,
)

/**
 * A zero-wrap overlay container for AELog.
 *
 * Drop this as a sibling anywhere in your composition tree — no content wrapping required.
 * The notch trigger and the panel render via system-level [Popup]s, so they appear
 * above your content without needing to be a layout parent.
 *
 * The floating notch trigger can be dragged and positioned anywhere on screen,
 * staying in place wherever it is dropped.
 *
 * ## Hiding the Notch / Custom Triggers
 * If you want to hide the floating notch trigger (e.g. to avoid overlapping with
 * your app's top bar or notch), but still keep the library fully active and record
 * network requests/logs, set [showNotch] to `false`.
 *
 * You can then open the overlay panel programmatically from a custom debug gesture,
 * a developer-only settings button, or a shake trigger:
 *
 * ```kotlin
 * @Composable
 * fun App() {
 *     // Renders AELog but hides the floating notch trigger
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
 * @param showNotch Whether to display the floating notch trigger.
 */
@Composable
public fun AELogOverlay(showNotch: Boolean = true) {
    val isEnabled by AELog.isEnabledFlow.collectAsState()
    val isNotchEnabledGlobal by AELog.showNotchFlow.collectAsState()
    val instance = AELog.instance
    if ((!isEnabled) || (instance == null)) return

    val controller =
        remember {
            LogController(
                backing = instance.overlayVisible,
                themeBacking = instance.themeMode,
            )
        }
    val isVisible by controller.isVisible.collectAsState()

    val plugins by instance.plugins.plugins.collectAsState()
    val uiPlugins = remember(plugins) { plugins.filterIsInstance<UIPlugin>() }
    val themeMode by controller.themeMode.collectAsState()

    CompositionLocalProvider(LocalLogController provides controller) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLargeScreen = (maxWidth > LogDimens.largeScreenBreakpoint) && (maxHeight > 480.dp)

            LogTheme(themeMode = themeMode) {
                // Notch pill — floating trigger movable anywhere on the screen.
                // Hidden while the panel is open so it doesn't overlap.
                if (!isVisible && showNotch && isNotchEnabledGlobal) {
                    val density = LocalDensity.current

                    val dim =
                        remember(maxWidth, maxHeight, density) {
                            val screenWidthPx = with(density) { maxWidth.toPx() }
                            val screenHeightPx = with(density) { maxHeight.toPx() }
                            val buttonWidthPx = with(density) { 36.dp.toPx() }
                            val buttonHeightPx = with(density) { 120.dp.toPx() }

                            val maxX = (screenWidthPx - buttonWidthPx).coerceAtLeast(0f)
                            val maxY = (screenHeightPx - buttonHeightPx).coerceAtLeast(0f)

                            OverlayNotchDimensions(
                                maxX = maxX,
                                maxY = maxY,
                                defaultX = maxX,
                                defaultY = (maxY / 2f).coerceAtLeast(0f),
                            )
                        }

                    val animX = remember { Animatable(dim.defaultX) }
                    val animY = remember { Animatable(dim.defaultY) }
                    val coroutineScope = rememberCoroutineScope()

                    // Ensure position stays within screen boundaries on screen resize / rotation
                    LaunchedEffect(dim) {
                        if (animX.value > dim.maxX) animX.snapTo(dim.maxX)
                        if (animY.value > dim.maxY) animY.snapTo(dim.maxY)
                    }

                    val clampedX = animX.value.coerceIn(0f, dim.maxX)
                    val clampedY = animY.value.coerceIn(0f, dim.maxY)

                    Popup(
                        alignment = Alignment.TopStart,
                        offset =
                            IntOffset(
                                clampedX.roundToInt(),
                                clampedY.roundToInt(),
                            ),
                        properties = PopupProperties(focusable = false),
                    ) {
                        LogNotchButton(
                            onClick = { controller.show() },
                            onDrag = { dragAmount ->
                                coroutineScope.launch {
                                    val newX = (animX.value + dragAmount.x).coerceIn(0f, dim.maxX)
                                    val newY = (animY.value + dragAmount.y).coerceIn(0f, dim.maxY)
                                    animX.snapTo(newX)
                                    animY.snapTo(newY)
                                }
                            },
                            onDragEnd = {
                                coroutineScope.launch {
                                    val cX = animX.value.coerceIn(0f, dim.maxX)
                                    val cY = animY.value.coerceIn(0f, dim.maxY)
                                    animX.snapTo(cX)
                                    animY.snapTo(cY)
                                }
                            },
                            themeMode = themeMode,
                        )
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
}
