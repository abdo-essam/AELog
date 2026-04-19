package com.ae.devlens.core

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Base interface for all DevLens plugins.
 *
 * Plugins provide modular, isolated functionality to DevLens.
 * Both [UIPlugin] (with Compose UI) and [DataPlugin] (headless) extend this.
 *
 * ## Lifecycle
 * ```
 * install() → onAttach() → onStart() → onOpen() ⇄ onClose() → onStop() → onDetach()
 * ```
 *
 * | Callback | Trigger                                         |
 * |----------|-------------------------------------------------|
 * | onAttach | Plugin registered with DevLens (once)           |
 * | onStart  | Host app moved to foreground                    |
 * | onOpen   | DevLens UI panel opened                         |
 * | onClose  | DevLens UI panel closed                         |
 * | onStop   | Host app moved to background                    |
 * | onDetach | Plugin unregistered (once)                      |
 * | onClear  | User requested a data wipe                      |
 */
public interface DevLensPlugin {
    /** Unique identifier for this plugin (must be stable across restarts) */
    public val id: String

    /** Display name shown in tabs and headers */
    public val name: String

    /**
     * Badge count displayed on the plugin tab.
     *
     * - `null` → no badge rendered
     * - `0`    → badge shown with "0"
     *
     * Always back this with a stable property — never return a new
     * [MutableStateFlow] from the getter, or UI collectors will miss updates.
     *
     * ```kotlin
     * private val _badgeCount = MutableStateFlow<Int?>(null)
     * override val badgeCount: StateFlow<Int?> = _badgeCount
     * ```
     */
    public val badgeCount: StateFlow<Int?>
        get() = NoBadge

    /**
     * Called once when the plugin is registered.
     *
     * Use [context] to launch coroutines, access config, or look up sibling plugins.
     * The [PluginContext.scope] is cancelled automatically before [onDetach].
     */
    public fun onAttach(context: PluginContext) {}

    /** Called when the host app comes to the foreground. Resume background tasks here. */
    public fun onStart() {}

    /** Called every time the DevLens UI panel is opened. */
    public fun onOpen() {}

    /** Called every time the DevLens UI panel is closed. */
    public fun onClose() {}

    /** Called when the host app goes to the background. Pause expensive tasks here. */
    public fun onStop() {}

    /** Called once when the plugin is unregistered. Scope is already cancelled. */
    public fun onDetach() {}

    /** Called when the user requests a full data wipe. */
    public fun onClear() {}

    public companion object {
        /**
         * Shared no-op badge flow returned by the default [badgeCount] getter.
         * Safe to share — it is never mutated.
         */
        public val NoBadge: StateFlow<Int?> = MutableStateFlow(null)
    }
}
