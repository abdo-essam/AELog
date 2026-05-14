package com.ae.log.plugin

import com.ae.log.event.AllDataClearedEvent
import com.ae.log.event.AppStartedEvent
import com.ae.log.event.AppStoppedEvent
import com.ae.log.event.EventBus
import com.ae.log.event.PanelClosedEvent
import com.ae.log.event.PanelOpenedEvent

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
