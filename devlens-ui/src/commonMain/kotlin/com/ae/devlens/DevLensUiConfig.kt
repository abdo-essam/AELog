package com.ae.devlens

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * UI-specific configuration for the DevLens overlay.
 *
 * Passed to [AEDevLensProvider] separately from the core [DevLensConfig],
 * keeping Compose types out of the `devlens-core` module.
 *
 * ```kotlin
 * AEDevLensProvider(
 *     inspector = inspector,
 *     uiConfig = DevLensUiConfig(
 *         showFloatingButton = true,
 *         presentationMode = PresentationMode.Adaptive,
 *     ),
 * ) { ... }
 * ```
 */
public data class DevLensUiConfig(
    /** Show the floating debug button overlay. Default: `true`. */
    val showFloatingButton: Boolean = true,
    /** Floating button screen position. Default: `BottomEnd`. */
    val floatingButtonAlignment: Alignment = Alignment.BottomEnd,
    /**
     * Bottom offset for the floating button — useful to clear nav bars or bottom bars.
     * Default: 80.dp (roughly matches a standard bottom navigation bar height).
     */
    val floatingButtonOffset: Dp = 80.dp,
    /** Enable long-press anywhere on screen to open the panel. Default: `true`. */
    val enableLongPress: Boolean = true,
    /**
     * Custom Material3 [ColorScheme] for the DevLens UI.
     * `null` uses the built-in brand theme.
     */
    val colorScheme: ColorScheme? = null,
    /**
     * How the DevLens panel is presented on screen.
     * Default: [PresentationMode.Adaptive] (bottom sheet on phone, dialog on tablet).
     */
    val presentationMode: PresentationMode = PresentationMode.Adaptive,
)

/**
 * Controls how the DevLens panel container is displayed.
 */
public enum class PresentationMode {
    /** Bottom sheet on compact screens, centered dialog on large screens (default). */
    Adaptive,

    /** Always use a bottom sheet, regardless of screen size. */
    BottomSheet,

    /** Always use a centered dialog, regardless of screen size. */
    Dialog,
}
