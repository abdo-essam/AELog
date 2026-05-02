package com.ae.log.core

/**
 * Base interface for all AELog plugins.
 *
 * Plugins provide modular, isolated functionality to AELog.
 * Both [UIPlugin] (with Compose UI) and [DataPlugin] (headless) extend this.
 *
 * ## Lifecycle
 * ```
 * install() → onAttach() → onStart() → onOpen() ⇄ onClose() → onStop() → onDetach()
 * ```
 *
 * | Callback | Trigger                                         |
 * |----------|-------------------------------------------------|
 * | onAttach | Plugin registered with AELog (once)           |
 * | onStart  | Host app moved to foreground                    |
 * | onOpen   | AELog UI panel opened                         |
 * | onClose  | AELog UI panel closed                         |
 * | onStop   | Host app moved to background                    |
 * | onDetach | Plugin unregistered (once)                      |
 * | onClear  | User requested a data wipe                      |
 */
public interface AELogPlugin {
    /** Unique identifier for this plugin (must be stable across restarts) */
    public val id: String

    /** Display name shown in tabs and headers */
    public val name: String

    /**
     * Called once when the plugin is registered.
     *
     * Use [context] to launch coroutines, access config, or look up sibling plugins.
     * The [PluginContext.scope] is cancelled automatically before [onDetach].
     */
    public fun onAttach(context: PluginContext) {}

    /** Called when the host app comes to the foreground. Resume background tasks here. */
    public fun onStart() {}

    /** Called every time the AELog UI panel is opened. */
    public fun onOpen() {}

    /** Called every time the AELog UI panel is closed. */
    public fun onClose() {}

    /** Called when the host app goes to the background. Pause expensive tasks here. */
    public fun onStop() {}

    /** Called once when the plugin is unregistered. Scope is already cancelled. */
    public fun onDetach() {}

    /** Called when the user requests a full data wipe. */
    public fun onClear() {}

    /**
     * Export the plugin's data as a formatted string.
     * Use this to attach data to bug reports or logs.
     */
    public fun export(): String = ""
}
