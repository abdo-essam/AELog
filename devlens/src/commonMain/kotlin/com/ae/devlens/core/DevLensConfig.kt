package com.ae.devlens.core

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.Alignment

/**
 * Configuration for a [com.ae.devlens.AEDevLens] instance.
 *
 * ```kotlin
 * AEDevLens.create(DevLensConfig(
 *     maxLogEntries = 1000,
 *     showFloatingButton = true,
 * ))
 * ```
 */
public data class DevLensConfig(
    /** Maximum number of log entries to keep in memory (default: 500) */
    val maxLogEntries: Int = 500,
    /** Show the floating debug button overlay (default: true) */
    val showFloatingButton: Boolean = true,
    /** Floating button position (default: BottomEnd) */
    val floatingButtonAlignment: Alignment = Alignment.BottomEnd,
    /**
     * Enable long-press gesture to open inspector.
     * Uses Compose's default long-press duration.
     */
    val enableLongPress: Boolean = true,
    /**
     * Custom Material3 [ColorScheme] for the DevLens UI.
     * Null uses the built-in theme.
     */
    val colorScheme: ColorScheme? = null,
)
