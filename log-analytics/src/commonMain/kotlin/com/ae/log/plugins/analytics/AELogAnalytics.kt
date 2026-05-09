
package com.ae.log.plugins.analytics

import com.ae.log.AELog

/**
 * Access analytics tools.
 *
 * This provides a non-null entry point to the analytics system.
 * If the [AnalyticsPlugin] is not installed, these methods silently no-op.
 */
public val AELog.analytics: AnalyticsProxy
    get() = AnalyticsProxy

/**
 * Proxy that hides the internal nullability of the plugin system.
 */
public object AnalyticsProxy {
    /**
     * Track a custom event.
     */
    public fun logEvent(
        name: String,
        properties: Map<String, String> = emptyMap(),
    ) {
        AELog.getPlugin<AnalyticsPlugin>()?.tracker?.track(name, properties)
    }

    /**
     * Track a screen view.
     */
    public fun logScreen(
        screenName: String,
        properties: Map<String, String> = emptyMap(),
    ) {
        AELog.getPlugin<AnalyticsPlugin>()?.tracker?.screen(screenName, properties)
    }

    /**
     * Clear all recorded analytics events.
     */
    public fun clear() {
        AELog.getPlugin<AnalyticsPlugin>()?.tracker?.clear()
    }
}
