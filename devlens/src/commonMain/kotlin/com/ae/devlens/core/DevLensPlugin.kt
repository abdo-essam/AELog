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
 * | Callback    | Trigger                                          |
 * |-------------|--------------------------------------------------|
 * | onAttach    | Plugin registered with DevLens (once)            |
 * | onStart     | Host app moved to foreground                     |
 * | onOpen      | DevLens UI panel opened                          |
 * | onClose     | DevLens UI panel closed                          |
 * | onStop      | Host app moved to background                     |
 * | onDetach    | Plugin unregistered (once)                       |
 * | onClear     | User requested data wipe                         |
 */
public interface DevLensPlugin {

    /** Unique identifier for this plugin (must be stable across restarts) */
    public val id: String

    /** Display name (shown in tabs, headers) */
    public val name: String

    /**
     * Badge count displayed on the plugin tab.
     *
     * - `null` → no badge rendered
     * - `0`    → badge shown with "0"
     *
     * **Implementation note:** always back this with a stable property,
     * never return a new [MutableStateFlow] from the getter — UI collectors
     * would observe different instances and never see updates.
     *
     * ```kotlin
     * private val _badgeCount = MutableStateFlow<Int?>(null)
     * override val badgeCount: StateFlow<Int?> = _badgeCount
     * ```
     */
    public val badgeCount: StateFlow<Int?>
        get() = NoBadge

    /**
     * Called once when the plugin is registered with DevLens.
     *
     * Use [context] to launch coroutines, read config, or look up sibling plugins.
     * The [context.scope] is cancelled automatically when [onDetach] is called —
     * no manual cleanup needed for coroutines started here.
     */
    public fun onAttach(context: PluginContext) {}

    /**
     * Called when the host application comes to the foreground.
     *
     * Use this to resume background sampling, polling, or data collection
     * that should only run while the app is visible.
     */
    public fun onStart() {}

    /** Called every time the DevLens UI panel is opened */
    public fun onOpen() {}

    /** Called every time the DevLens UI panel is closed */
    public fun onClose() {}

    /**
     * Called when the host application goes to the background.
     *
     * Counterpart to [onStart] — pause expensive operations here.
     */
    public fun onStop() {}

    /** Called once when the plugin is unregistered. Coroutines from [onAttach] are already cancelled. */
    public fun onDetach() {}

    /** Called when the user requests a full data wipe for this plugin */
    public fun onClear() {}

    public companion object {
        /**
         * Shared singleton StateFlow for plugins that never show a badge.
         *
         * Returned by the default [badgeCount] getter — safe to share across
         * instances because it is never mutated.
         */
        public val NoBadge: StateFlow<Int?> = MutableStateFlow(null)
    }
}
