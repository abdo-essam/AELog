package com.ae.log

import com.ae.log.event.AllDataClearedEvent
import com.ae.log.event.AppStartedEvent
import com.ae.log.event.AppStoppedEvent
import com.ae.log.event.EventBus
import com.ae.log.event.PanelClosedEvent
import com.ae.log.event.PanelOpenedEvent
import com.ae.log.plugin.PluginManager

/**
 * Publishes library-wide lifecycle signals to the central [EventBus] and drives
 * plugin data operations.
 *
 * Plugins that need to react to these signals should subscribe via
 * [com.ae.log.plugin.PluginContext.collectEvents] inside their [com.ae.log.plugin.Plugin.onAttach].
 */
internal class AELogLifecycle(
    private val pluginManager: PluginManager,
    private val eventBus: EventBus,
) {
    fun notifyStart() {
        eventBus.publish(AppStartedEvent)
    }

    fun notifyStop() {
        eventBus.publish(AppStoppedEvent)
    }

    fun notifyOpen() {
        eventBus.publish(PanelOpenedEvent)
    }

    fun notifyClose() {
        eventBus.publish(PanelClosedEvent)
    }

    fun clearAll() {
        pluginManager.forEach { it.onClear() }
        eventBus.publish(AllDataClearedEvent)
    }
}
