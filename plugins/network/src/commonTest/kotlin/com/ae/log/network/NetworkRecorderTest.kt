package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.network.model.NetworkMethod
import com.ae.log.network.storage.NetworkStorage
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class NetworkRecorderTest {

    private lateinit var storage: NetworkStorage
    private lateinit var recorder: NetworkRecorder

    @BeforeTest
    fun setUp() {
        AELog.init(NetworkPlugin())
        storage = NetworkStorage()
        recorder = NetworkRecorder(storage)
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    @Test
    fun `logRequest - stores entry in storage`() {
        recorder.logRequest(method = "GET", url = "https://example.com")
        assertEquals(1, storage.entries.value.size)
        assertEquals("https://example.com", storage.entries.value.first().url)
        assertEquals(NetworkMethod.GET, storage.entries.value.first().method)
    }

    @Test
    fun `logRequest - records status code when provided`() {
        recorder.logRequest(method = "POST", url = "https://api.example.com", statusCode = 201)
        assertEquals(201, storage.entries.value.first().statusCode)
    }

    @Test
    fun `startRequest and logResponse - pairs correctly by id`() {
        val id = recorder.newId()
        recorder.startRequest(id = id, url = "https://example.com", method = NetworkMethod.GET)

        // Entry should be pending initially
        val pending = storage.entries.value.first()
        assertNull(pending.statusCode)
        assertTrue(pending.isPending)

        recorder.logResponse(id = id, statusCode = 200, durationMs = 150L)

        val completed = storage.entries.value.first()
        assertEquals(200, completed.statusCode)
        assertEquals(150L, completed.durationMs)
        assertTrue(completed.isSuccess)
    }

    @Test
    fun `logError - sets error message on entry`() {
        val id = recorder.newId()
        recorder.startRequest(id = id, url = "https://example.com", method = NetworkMethod.GET)
        recorder.logError(id = id, message = "Connection refused")

        val entry = storage.entries.value.first()
        assertEquals("Connection refused", entry.error)
        assertTrue(entry.isError)
    }

    @Test
    fun `updateResponseBody - patches body after logging response`() {
        val id = recorder.newId()
        recorder.startRequest(id = id, url = "https://example.com", method = NetworkMethod.GET)
        recorder.logResponse(id = id, statusCode = 200)
        recorder.updateResponseBody(id = id, body = "{\"ok\":true}")

        assertEquals("{\"ok\":true}", storage.entries.value.first().responseBody)
    }

    @Test
    fun `updateRequestBody - patches request body`() {
        val id = recorder.newId()
        recorder.startRequest(id = id, url = "https://example.com", method = NetworkMethod.POST)
        recorder.updateRequestBody(id = id, body = "{\"name\":\"test\"}")

        assertEquals("{\"name\":\"test\"}", storage.entries.value.first().requestBody)
    }

    @Test
    fun `clear - empties storage`() {
        recorder.logRequest(method = "GET", url = "https://example.com")
        recorder.clear()
        assertTrue(storage.entries.value.isEmpty())
    }

    @Test
    fun `newId - returns unique ids`() {
        val ids = (1..100).map { recorder.newId() }.toSet()
        assertEquals(100, ids.size)
    }

    @Test
    fun `logRequest - is no-op when AELog isEnabled is false`() {
        AELog.isEnabled = false
        recorder.logRequest(method = "GET", url = "https://example.com")
        assertTrue(storage.entries.value.isEmpty())
    }
}
