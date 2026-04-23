package com.ae.devlens.core.bus

/**
 * Marker interface for all DevLens events flowing through the [EventBus].
 *
 * Define custom events by implementing this interface:
 * ```kotlin
 * data class MyPluginEvent(val data: String) : DevLensEvent
 * ```
 *
 * Built-in events are provided for common cross-plugin signals.
 */
public interface DevLensEvent

/** Fired by DevLens when the panel UI is opened. */
public object PanelOpenedEvent : DevLensEvent

/** Fired by DevLens when the panel UI is closed. */
public object PanelClosedEvent : DevLensEvent

/** Fired by DevLens when the host app comes to the foreground. */
public object AppStartedEvent : DevLensEvent

/** Fired by DevLens when the host app goes to the background. */
public object AppStoppedEvent : DevLensEvent

/** Fired after [com.ae.devlens.AEDevLens.clearAll] — signals all plugins to reset their state. */
public object AllDataClearedEvent : DevLensEvent

/** Fired by plugins to register their custom tags with the logs viewer dynamically. */
public data class RegisterLogTagEvent(
    val tag: String,
    val badgeLabel: String,
) : DevLensEvent
