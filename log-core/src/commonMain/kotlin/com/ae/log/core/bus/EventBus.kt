package com.ae.log.core.bus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filterIsInstance

public class EventBus {
    private val _events = MutableSharedFlow<Event>(extraBufferCapacity = 64)
    public val events: SharedFlow<Event> = _events.asSharedFlow()

    public fun publish(event: Event) {
        _events.tryEmit(event)
    }

    public suspend fun publishSuspend(event: Event) {
        _events.emit(event)
    }
}

public inline fun <reified T : Event> EventBus.subscribe(): kotlinx.coroutines.flow.Flow<T> =
    events.filterIsInstance<T>()
