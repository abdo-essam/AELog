package com.ae.log.plugin

import com.ae.log.core.event.AllDataClearedEvent
import com.ae.log.core.event.AppStartedEvent
import com.ae.log.core.event.AppStoppedEvent
import com.ae.log.core.event.EventBus
import com.ae.log.core.event.PanelClosedEvent
import com.ae.log.core.event.PanelOpenedEvent

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
