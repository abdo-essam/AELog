package com.ae.log.ui.theme

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Common spacing scale used throughout the AELog UI. */
public object LogSpacing {
    public val x1: Dp = 4.dp
    public val x1_5: Dp = 6.dp
    public val x2: Dp = 8.dp
    public val x3: Dp = 12.dp
    public val x4: Dp = 16.dp
    public val x5: Dp = 20.dp
    public val x6: Dp = 24.dp
    public val x8: Dp = 32.dp
    public val x10: Dp = 40.dp
    public val x12: Dp = 48.dp
}

/** Named dimension constants used throughout the AELog UI. */
public object LogDimens {
    /** Width threshold above which the panel switches from bottom-sheet to dialog. */
    public val largeScreenBreakpoint: Dp = 600.dp

    /** Corner radius used on dialog and bottom-sheet overlays. */
    public val overlayCornerRadius: Dp = LogSpacing.x6

    /** Tonal elevation for the dialog overlay surface. */
    public val dialogTonalElevation: Dp = 6.dp

    /** Thickness of the divider between list items. */
    public val listDividerThickness: Dp = 1.dp
}
