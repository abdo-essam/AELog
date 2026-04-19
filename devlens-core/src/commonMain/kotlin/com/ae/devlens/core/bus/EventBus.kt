package com.ae.devlens.core.bus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Lightweight, coroutine-based publish/subscribe event bus for cross-plugin communication.
 *
 * Plugins receive an [EventBus] via [com.ae.devlens.core.PluginContext.eventBus].
 * Events are delivered asynchronously on the subscriber's coroutine scope.
 *
 * ## Publishing an event
 * ```kotlin
 * override fun onAttach(context: PluginContext) {
 *     context.scope.launch {
 *         context.eventBus.publish(MyCustomEvent("hello"))
 *     }
 * }
 * ```
 *
 * ## Subscribing to events
 * ```kotlin
 * override fun onAttach(context: PluginContext) {
 *     context.scope.launch {
 *         context.eventBus.events
 *             .filterIsInstance<MyCustomEvent>()
 *             .collect { event -> handleEvent(event) }
 *     }
 * }
 * ```
 */
public class EventBus {
    private val _events =
        MutableSharedFlow<DevLensEvent>(
            extraBufferCapacity = 64,
        )

    /**
     * Hot stream of all events published to this bus.
     *
     * Use [kotlinx.coroutines.flow.filterIsInstance] to receive only specific event types.
     */
    public val events: SharedFlow<DevLensEvent> = _events.asSharedFlow()

    /**
     * Publish an event to all current subscribers.
     *
     * This is a fire-and-forget call — it never suspends, but may drop the event
     * if no subscriber is listening and the internal buffer (64 slots) is full.
     */
    public fun publish(event: DevLensEvent) {
        _events.tryEmit(event)
    }

    /**
     * Publish an event, suspending until it is delivered to at least one subscriber.
     *
     * Use this from a coroutine when delivery guarantee matters.
     */
    public suspend fun publishSuspend(event: DevLensEvent) {
        _events.emit(event)
    }
}
