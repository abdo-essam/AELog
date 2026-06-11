package com.ae.log.plugin

public interface Plugin {
    /**
     * Stable, unique identifier for this plugin.
     *
     * Defaults to the fully-qualified class name so most plugins need not
     * override this. Override only when using anonymous or local classes, or
     * when you intentionally want a fixed string that differs from the class name.
     */
    public val id: String get() =
        this::class.qualifiedName
            ?: error("Plugin must override 'id' — anonymous/local classes have no stable qualified name.")

    /** Human-readable label shown in the AELog panel tab. */
    public val name: String

    /**
     * Called once when this plugin is registered with [com.ae.log.AELog].
     * Use this to initialise coroutines or wire up storage.
     * The provided [PluginContext] gives access to the shared [kotlinx.coroutines.CoroutineScope]
     * and [com.ae.log.config.LogConfig].
     */
    public fun onAttach(context: PluginContext) {}

    /**
     * Called when this plugin is permanently removed from [com.ae.log.AELog].
     * Release any resources held since [onAttach].
     */
    public fun onDetach() {}

    /**
     * Called when the user or host requests all captured data to be discarded.
     * Clear any in-memory or persisted entries here.
     */
    public fun onClear() {}

    /**
     * Returns a plain-text snapshot of this plugin's current data, used by
     * [com.ae.log.AELog.export]. Return an empty string if there is nothing to export.
     */
    public fun export(): String = ""
}
