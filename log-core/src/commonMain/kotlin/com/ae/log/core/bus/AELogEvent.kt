package com.ae.log.core.bus

/**
 * Marker interface for all AELog events flowing through the [EventBus].
 *
 * Define custom events by implementing this interface:
 * ```kotlin
 * data class MyPluginEvent(val data: String) : AELogEvent
 * ```
 *
 * Built-in events are provided for common cross-plugin signals.
 */
public interface AELogEvent

/** Fired by AELog when the panel UI is opened. */
public object PanelOpenedEvent : AELogEvent

/** Fired by AELog when the panel UI is closed. */
public object PanelClosedEvent : AELogEvent

/** Fired by AELog when the host app comes to the foreground. */
public object AppStartedEvent : AELogEvent

/** Fired by AELog when the host app goes to the background. */
public object AppStoppedEvent : AELogEvent

/** Fired after [com.ae.log.AELog.clearAll] — signals all plugins to reset their state. */
public object AllDataClearedEvent : AELogEvent

/** Fired by plugins to register their custom tags with the logs viewer dynamically. */
public data class RegisterLogTagEvent(
    val tag: String,
    val badgeLabel: String,
) : AELogEvent
