package com.ae.log

import com.ae.log.core.bus.AllDataClearedEvent
import com.ae.log.core.bus.AppStartedEvent
import com.ae.log.core.bus.AppStoppedEvent
import com.ae.log.core.bus.EventBus
import com.ae.log.core.bus.PanelClosedEvent
import com.ae.log.core.bus.PanelOpenedEvent

internal class Lifecycle(
    private val pluginManager: PluginManager,
    private val eventBus: EventBus,
) {
    fun notifyStart() {
        pluginManager.forEach { it.onStart() }
        eventBus.publish(AppStartedEvent)
    }

    fun notifyStop() {
        pluginManager.forEach { it.onStop() }
        eventBus.publish(AppStoppedEvent)
    }

    fun notifyOpen() {
        pluginManager.forEach { it.onOpen() }
        eventBus.publish(PanelOpenedEvent)
    }

    fun notifyClose() {
        pluginManager.forEach { it.onClose() }
        eventBus.publish(PanelClosedEvent)
    }

    fun clearAll() {
        pluginManager.forEach { it.onClear() }
        eventBus.publish(AllDataClearedEvent)
    }
}
