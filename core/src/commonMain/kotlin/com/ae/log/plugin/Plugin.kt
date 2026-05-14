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

    public fun export(): String = ""
}
