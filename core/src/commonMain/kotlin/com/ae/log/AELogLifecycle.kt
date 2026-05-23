package com.ae.log

import com.ae.log.event.AllDataClearedEvent
import com.ae.log.event.AppStartedEvent
import com.ae.log.event.AppStoppedEvent
import com.ae.log.event.EventBus
import com.ae.log.event.PanelClosedEvent
import com.ae.log.event.PanelOpenedEvent
import com.ae.log.plugin.PluginManager

/**
 * Orchestrates and propagates library-wide lifecycle events to all installed plugins
 * and publishes corresponding event signals to the central [EventBus].
 *
 * This keeps the plugin manager and event bus synchronized whenever high-level operations
 * occur (such as starting/stopping the SDK, opening/closing the UI overlay, or clearing all data).
 */
internal class AELogLifecycle(
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
