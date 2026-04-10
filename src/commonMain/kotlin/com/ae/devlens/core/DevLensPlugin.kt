package com.ae.devlens.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for all AEDevLens plugins.
 *
 * Plugins provide modular, isolated functionality to DevLens.
 * Both [UIPlugin] (with Compose UI) and [DataPlugin] (headless) extend this.
 *
 * ## Lifecycle
 * ```
 * install() → onAttach() → onOpen() ⇄ onClose() → onDetach()
 * ```
 */
interface DevLensPlugin {
    /** Unique identifier for this plugin */
    val id: String

    /** Display name (shown in tabs, headers) */
    val name: String

    /**
     * Badge count displayed on the plugin tab.
     * Null means no badge. 0 means badge with "0".
     */
    val badgeCount: StateFlow<Int?>
        get() = MutableStateFlow(null)

    /** Called once when the plugin is registered with DevLens */
    fun onAttach(devLens: com.ae.devlens.AEDevLens) {}

    /** Called every time the DevLens UI is opened */
    fun onOpen() {}

    /** Called every time the DevLens UI is closed */
    fun onClose() {}

    /** Called once when the plugin is unregistered */
    fun onDetach() {}

    /** Clear all data in this plugin */
    fun onClear() {}
}
