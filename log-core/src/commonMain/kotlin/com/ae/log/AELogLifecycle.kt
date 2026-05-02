package com.ae.log

import com.ae.log.core.bus.AllDataClearedEvent
import com.ae.log.core.bus.AppStartedEvent
import com.ae.log.core.bus.AppStoppedEvent
import com.ae.log.core.bus.EventBus
import com.ae.log.core.bus.PanelClosedEvent
import com.ae.log.core.bus.PanelOpenedEvent

/**
 * Manages lifecycle notifications for AELog plugins and event bus.
 */
public class AELogLifecycle internal constructor(
    private val pluginManager: AELogPluginManager,
    private val eventBus: EventBus,
) {
    /**
     * Notify all plugins the host app has moved to the **foreground**.
     * Publishes [AppStartedEvent] to [eventBus].
     */
    public fun notifyStart() {
        pluginManager.forEach { it.onStart() }
        eventBus.publish(AppStartedEvent)
    }

    /**
     * Notify all plugins the host app has moved to the **background**.
     * Publishes [AppStoppedEvent] to [eventBus].
     */
    public fun notifyStop() {
        pluginManager.forEach { it.onStop() }
        eventBus.publish(AppStoppedEvent)
    }

    /**
     * Notify all plugins the AELog UI panel has been **opened**.
     * Publishes [PanelOpenedEvent] to [eventBus].
     */
    public fun notifyOpen() {
        pluginManager.forEach { it.onOpen() }
        eventBus.publish(PanelOpenedEvent)
    }

    /**
     * Notify all plugins the AELog UI panel has been **closed**.
     * Publishes [PanelClosedEvent] to [eventBus].
     */
    public fun notifyClose() {
        pluginManager.forEach { it.onClose() }
        eventBus.publish(PanelClosedEvent)
    }

    /** Clear all plugin data and publish [AllDataClearedEvent]. */
    public fun clearAll() {
        pluginManager.forEach { it.onClear() }
        eventBus.publish(AllDataClearedEvent)
    }
}
