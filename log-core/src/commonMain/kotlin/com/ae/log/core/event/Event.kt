package com.ae.log.core.event

public interface Event

public object PanelOpenedEvent : Event

public object PanelClosedEvent : Event

public object AppStartedEvent : Event

public object AppStoppedEvent : Event

public object AllDataClearedEvent : Event

public data class LogTagRegisteredEvent(
    val tag: String,
    val badgeLabel: String,
) : Event
