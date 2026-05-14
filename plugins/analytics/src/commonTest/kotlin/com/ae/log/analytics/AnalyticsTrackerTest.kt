package com.ae.log.analytics

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.analytics.model.DefaultAdapterSource
import com.ae.log.analytics.storage.AnalyticsStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class AnalyticsTrackerTest {

    private lateinit var storage: AnalyticsStorage
    private lateinit var tracker: AnalyticsTracker

    @BeforeTest
    fun setUp() {
        AELog.init(AnalyticsPlugin())
        storage = AnalyticsStorage()
        tracker = AnalyticsTracker(storage)
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    @Test
    fun `track - stores event in storage`() {
        tracker.track("button_tap")
        assertEquals(1, storage.events.value.size)
        assertEquals("button_tap", storage.events.value.first().name)
    }

    @Test
    fun `track - records properties`() {
        tracker.track("purchase", mapOf("item" to "apple", "price" to "1.99"))
        val event = storage.events.value.first()
        assertEquals("apple", event.properties["item"])
        assertEquals("1.99", event.properties["price"])
    }

    @Test
    fun `track - records source adapter`() {
        tracker.track("event", source = DefaultAdapterSource.FIREBASE)
        val event = storage.events.value.first()
        assertEquals("Firebase", event.source?.sourceName)
    }

    @Test
    fun `track - assigns a non-blank id`() {
        tracker.track("test_event")
        assertTrue(storage.events.value.first().id.isNotBlank())
    }

    @Test
    fun `track - assigns a positive timestamp`() {
        tracker.track("test_event")
        assertTrue(storage.events.value.first().timestamp > 0)
    }

    @Test
    fun `track - is no-op when AELog isEnabled is false`() {
        AELog.isEnabled = false
        tracker.track("suppressed_event")
        assertTrue(storage.events.value.isEmpty())
    }

    @Test
    fun `screen - tracks screen_view event with screen property`() {
        tracker.screen("HomeScreen", mapOf("source" to "deeplink"))
        val event = storage.events.value.first()
        assertEquals("screen_view", event.name)
        assertEquals("HomeScreen", event.properties["screen"])
        assertEquals("deeplink", event.properties["source"])
    }

    @Test
    fun `clear - empties storage`() {
        tracker.track("event_1")
        tracker.track("event_2")
        tracker.clear()
        assertTrue(storage.events.value.isEmpty())
    }

    @Test
    fun `multiple events - preserved in insertion order`() {
        tracker.track("first")
        tracker.track("second")
        tracker.track("third")
        val names = storage.events.value.map { it.name }
        assertEquals(listOf("first", "second", "third"), names)
    }
}
