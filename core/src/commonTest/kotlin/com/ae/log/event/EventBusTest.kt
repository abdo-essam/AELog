package com.ae.log.event

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EventBusTest {
    private val bus = EventBus()

    // ── Publish / subscribe ───────────────────────────────────────────────

    @Test
    fun `publish - emits event to subscribers`() =
        runTest {
            bus.events.test {
                bus.publish(AppStartedEvent)
                assertEquals(AppStartedEvent, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `subscribe - filters to specific event type`() =
        runTest {
            bus.subscribe<AppStartedEvent>().test {
                bus.publish(AppStoppedEvent) // should not arrive
                bus.publish(AppStartedEvent) // should arrive
                assertEquals(AppStartedEvent, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `publish - multiple events arrive in order`() =
        runTest {
            bus.events.test {
                bus.publish(AppStartedEvent)
                bus.publish(PanelOpenedEvent)
                bus.publish(PanelClosedEvent)
                assertEquals(AppStartedEvent, awaitItem())
                assertEquals(PanelOpenedEvent, awaitItem())
                assertEquals(PanelClosedEvent, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `tryEmit - does not block when no subscribers`() {
        // Should not throw or deadlock
        repeat(70) { bus.publish(AppStartedEvent) } // exceeds BUFFER_CAPACITY (64) — fine because tryEmit drops
        assertTrue(true, "No exception or deadlock occurred")
    }

    @Test
    fun `publishSuspend - delivers event`() =
        runTest {
            bus.events.test {
                bus.publishSuspend(AllDataClearedEvent)
                assertEquals(AllDataClearedEvent, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
}
