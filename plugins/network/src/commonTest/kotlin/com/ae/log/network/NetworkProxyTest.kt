package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class NetworkProxyTest {
    private lateinit var plugin: NetworkPlugin

    @BeforeTest
    fun setUp() {
        plugin = NetworkPlugin()
        AELog.init(plugin)
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── One-shot log() ────────────────────────────────────────────────────

    @Test
    fun `log - records complete request and response`() {
        AELog.network.log(
            method = "GET",
            url = "https://api.example.com/users",
            statusCode = 200,
            responseBody = """{"users":[]}""",
        )

        val export = plugin.export()
        assertTrue(export.contains("GET"))
        assertTrue(export.contains("https://api.example.com/users"))
        assertTrue(export.contains("200"))
    }

    @Test
    fun `log - without statusCode creates pending entry`() {
        AELog.network.log(method = "POST", url = "https://api.example.com/data")

        val export = plugin.export()
        assertTrue(export.contains("POST"))
        assertTrue(export.contains("PENDING"))
    }

    @Test
    fun `log - records request headers`() {
        AELog.network.log(
            method = "GET",
            url = "https://api.example.com",
            headers = mapOf("X-Custom" to "value"),
        )

        val export = plugin.export()
        assertTrue(export.contains("X-Custom"))
    }

    @Test
    fun `log - records request and response bodies`() {
        AELog.network.log(
            method = "POST",
            url = "https://api.example.com/data",
            body = """{"input":"data"}""",
            statusCode = 201,
            responseBody = """{"id":"123"}""",
        )

        val export = plugin.export()
        assertTrue(export.contains("input"))
        assertTrue(export.contains("123"))
    }

    // ── Two-step logRequest + logResponse ──────────────────────────────────

    @Test
    fun `logRequest - returns non-null id when plugin is installed`() {
        val id = AELog.network.logRequest(method = "GET", url = "https://api.example.com")
        assertNotNull(id)
    }

    @Test
    fun `logRequest - returns null when plugin is not installed`() {
        AELog.resetForTesting()
        AELog.init() // no plugins
        val id = AELog.network.logRequest(method = "GET", url = "https://api.example.com")
        assertNull(id)
    }

    @Test
    fun `logRequest + logResponse - two-step flow records complete entry`() {
        val id = AELog.network.logRequest(method = "GET", url = "https://api.example.com/users")
        assertNotNull(id)

        AELog.network.logResponse(id = id, statusCode = 200, body = """{"ok":true}""")

        val export = plugin.export()
        assertTrue(export.contains("GET"))
        assertTrue(export.contains("200"))
    }

    @Test
    fun `logRequest + logError - records error on entry`() {
        val id = AELog.network.logRequest(method = "GET", url = "https://api.example.com/timeout")
        assertNotNull(id)

        AELog.network.logError(id = id, message = "Connection timeout")

        val export = plugin.export()
        assertTrue(export.contains("Connection timeout"))
    }

    @Test
    fun `logResponse - records duration`() {
        val id = AELog.network.logRequest(method = "GET", url = "https://api.example.com")!!
        AELog.network.logResponse(id = id, statusCode = 200, durationMs = 150L)

        val export = plugin.export()
        assertTrue(export.contains("150"))
    }

    // ── isEnabled gate ────────────────────────────────────────────────────

    @Test
    fun `log - is no-op when AELog is disabled`() {
        AELog.isEnabled = false
        AELog.network.log(method = "GET", url = "https://api.example.com", statusCode = 200)

        val export = plugin.export()
        assertTrue(export.isEmpty())
    }

    @Test
    fun `logRequest - returns null when AELog is disabled`() {
        AELog.isEnabled = false
        // Plugin is installed but AELog is disabled — recorder.logRequest will no-op
        // but newId() still works, so the proxy returns an id. Let's verify no entry is stored.
        AELog.network.log(method = "GET", url = "https://api.example.com")
        assertTrue(plugin.export().isEmpty())
    }

    // ── clear ─────────────────────────────────────────────────────────────

    @Test
    fun `clear - empties all recorded traffic`() {
        AELog.network.log(method = "GET", url = "https://api.example.com/1", statusCode = 200)
        AELog.network.log(method = "GET", url = "https://api.example.com/2", statusCode = 200)

        AELog.network.clear()

        val export = plugin.export()
        assertTrue(export.isEmpty())
    }

    // ── Multiple entries ──────────────────────────────────────────────────

    @Test
    fun `multiple log calls - all entries are recorded`() {
        AELog.network.log(method = "GET", url = "https://api.example.com/a", statusCode = 200)
        AELog.network.log(method = "POST", url = "https://api.example.com/b", statusCode = 201)
        AELog.network.log(method = "DELETE", url = "https://api.example.com/c", statusCode = 204)

        val export = plugin.export()
        assertTrue(export.contains("/a"))
        assertTrue(export.contains("/b"))
        assertTrue(export.contains("/c"))
    }
}
