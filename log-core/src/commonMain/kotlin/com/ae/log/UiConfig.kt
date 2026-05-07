package com.ae.log

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

/**
 * UI-specific configuration for the AELog overlay.
 *
 * ```kotlin
 * LogProvider(
 *     uiConfig = UiConfig(
 *         showFloatingButton = true,
 *         presentationMode = PresentationMode.Adaptive,
 *     ),
 * ) { ... }
 * ```
 */
public data class UiConfig(
    /** Show the floating debug button overlay. Default: `true`. */
    val showFloatingButton: Boolean = true,
    /** Floating button screen position. Default: `BottomEnd`. */
    val floatingButtonAlignment: Alignment = Alignment.BottomEnd,
    /** Enable long-press anywhere on screen to open the panel. Default: `false`. */
    val enableLongPress: Boolean = false,
    /**
     * Custom Material3 [ColorScheme] for the AELog UI.
     * `null` uses the built-in brand theme.
     */
    val colorScheme: ColorScheme? = null,
    /**
     * How the AELog panel is presented on screen.
     * Default: [PresentationMode.Adaptive] (bottom sheet on phone, dialog on tablet).
     */
    val presentationMode: PresentationMode = PresentationMode.Adaptive,
    /** Height fraction for the bottom sheet (0.0–1.0). Default: 0.9. */
    val bottomSheetHeightFraction: Float = 0.9f,
    /** Size fraction for the dialog (width, height) (0.0–1.0). Default: (0.85, 0.8). */
    val dialogSizeFraction: Pair<Float, Float> = 0.85f to 0.8f,
)

public enum class PresentationMode {
    /** Bottom sheet on compact screens, dialog on large screens (default). */
    Adaptive,
    /** Always use a bottom sheet. */
    BottomSheet,
    /** Always use a centered dialog. */
    Dialog,
}
