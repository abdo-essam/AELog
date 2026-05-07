package com.ae.log

import com.ae.log.core.bus.AllDataClearedEvent
import com.ae.log.core.bus.AppStartedEvent
import com.ae.log.core.bus.AppStoppedEvent
import com.ae.log.core.bus.EventBus
import com.ae.log.core.bus.PanelClosedEvent
import com.ae.log.core.bus.PanelOpenedEvent

public class Lifecycle internal constructor(
    private val pluginManager: PluginManager,
    private val eventBus: EventBus,
) {
    public fun notifyStart() { pluginManager.forEach { it.onStart() }; eventBus.publish(AppStartedEvent) }
    public fun notifyStop() { pluginManager.forEach { it.onStop() }; eventBus.publish(AppStoppedEvent) }
    public fun notifyOpen() { pluginManager.forEach { it.onOpen() }; eventBus.publish(PanelOpenedEvent) }
    public fun notifyClose() { pluginManager.forEach { it.onClose() }; eventBus.publish(PanelClosedEvent) }
    public fun clearAll() { pluginManager.forEach { it.onClear() }; eventBus.publish(AllDataClearedEvent) }
}
