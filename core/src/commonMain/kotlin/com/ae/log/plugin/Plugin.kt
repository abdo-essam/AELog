package com.ae.log.plugin

public interface Plugin {
    public val id: String
    public val name: String

    public fun onAttach(context: PluginContext) {}

    public fun onStart() {}

    public fun onOpen() {}

    public fun onClose() {}

    public fun onStop() {}

    public fun onDetach() {}

    public fun onClear() {}

    /**
     * Called when this plugin is replacing a pre-existing plugin instance (e.g. during custom
     * configuration in `AELog.configure`). Use this to copy accumulated state or data.
     */
    public fun onMigrateFrom(oldPlugin: Plugin) {}

    public fun export(): String = ""
}
