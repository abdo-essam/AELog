package com.ae.log.ktor

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.network.NetworkPlugin
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class AELogKtorInterceptorTest {

    private lateinit var networkPlugin: NetworkPlugin

    @BeforeTest
    fun setUp() {
        networkPlugin = NetworkPlugin()
        AELog.init(networkPlugin)
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun mockClient(
        status: HttpStatusCode = HttpStatusCode.OK,
        contentType: ContentType = ContentType.Application.Json,
        responseBody: String = """{"ok":true}""",
        config: AELogKtorInterceptor.Config.() -> Unit = {},
    ): HttpClient = HttpClient(MockEngine) {
        install(KtorInterceptor, config)
        engine {
            addHandler {
                respond(
                    content = responseBody,
                    status = status,
                    headers = headersOf(HttpHeaders.ContentType, contentType.toString()),
                )
            }
        }
    }

    // ── Basic recording ───────────────────────────────────────────────────

    @Test
    fun `records GET request url and method`() = runTest {
        val client = mockClient()
        client.get("https://api.example.com/users")
        client.close()

        val export = networkPlugin.export()
        assertTrue(export.contains("GET"))
        assertTrue(export.contains("https://api.example.com/users"))
    }

    @Test
    fun `records POST request`() = runTest {
        val client = mockClient()
        client.post("https://api.example.com/data") {
            contentType(ContentType.Application.Json)
            setBody("""{"name":"test"}""")
        }
        client.close()

        val export = networkPlugin.export()
        assertTrue(export.contains("POST"))
        assertTrue(export.contains("https://api.example.com/data"))
    }

    @Test
    fun `records response status code`() = runTest {
        val client = mockClient(status = HttpStatusCode.Created)
        client.get("https://api.example.com/resource")
        client.close()

        assertTrue(networkPlugin.export().contains("201"))
    }

    @Test
    fun `records response body for JSON content type`() = runTest {
        val client = mockClient(responseBody = """{"users":[]}""")
        val response = client.get("https://api.example.com/users")
        // Must consume the body to trigger the response body capture phase
        response.bodyAsText()
        client.close()

        assertTrue(networkPlugin.export().contains("users"))
    }

    // ── isEnabled gate ────────────────────────────────────────────────────

    @Test
    fun `does not record when AELog is disabled`() = runTest {
        AELog.isEnabled = false
        val client = mockClient()
        client.get("https://api.example.com/users")
        client.close()

        assertTrue(networkPlugin.export().isEmpty())
    }

    @Test
    fun `resumes recording after re-enabling`() = runTest {
        AELog.isEnabled = false
        val client = mockClient()
        client.get("https://api.example.com/skip")

        AELog.isEnabled = true
        client.get("https://api.example.com/capture")
        client.close()

        assertTrue(networkPlugin.export().contains("capture"))
    }

    // ── Error handling ────────────────────────────────────────────────────

    @Test
    fun `records connection errors`() = runTest {
        // Use a plain Exception — java.io.IOException is not available in KMP common
        val client = HttpClient(MockEngine) {
            install(KtorInterceptor)
            engine {
                addHandler { throw Exception("Connection refused") }
            }
        }

        val threw = try {
            client.get("https://api.example.com/fail")
            false
        } catch (_: Throwable) {
            true
        }
        client.close()

        assertTrue(threw)
        assertTrue(networkPlugin.export().contains("Connection refused"))
    }

    // ── Header exclusion ──────────────────────────────────────────────────

    @Test
    fun `excludes default sensitive headers`() = runTest {
        val client = mockClient()
        client.get("https://api.example.com/users") {
            header("Authorization", "Bearer secret-token")
            header("X-Custom", "visible")
        }
        client.close()

        // Authorization is excluded by InterceptorDefaults.DEFAULT_EXCLUDED
        assertTrue(!networkPlugin.export().contains("secret-token"))
    }

    // ── Body truncation ───────────────────────────────────────────────────

    @Test
    fun `truncates large response bodies`() = runTest {
        val largeBody = "x".repeat(300_000) // > 250 KB default
        val client = mockClient(responseBody = largeBody) {
            maxBodyBytes = 1000
        }
        val response = client.get("https://api.example.com/large")
        response.bodyAsText()
        client.close()

        assertTrue(networkPlugin.export().contains("[truncated]"))
    }

    // ── Binary content ────────────────────────────────────────────────────

    @Test
    fun `skips body capture for binary content types`() = runTest {
        val client = mockClient(
            contentType = ContentType.Image.PNG,
            responseBody = "binary-data",
        )
        val response = client.get("https://api.example.com/image.png")
        response.bodyAsText()
        client.close()

        assertTrue(!networkPlugin.export().contains("binary-data"))
    }

    // ── Multiple requests ─────────────────────────────────────────────────

    @Test
    fun `records multiple requests independently`() = runTest {
        val client = mockClient()
        client.get("https://api.example.com/first")
        client.get("https://api.example.com/second")
        client.get("https://api.example.com/third")
        client.close()

        val export = networkPlugin.export()
        assertTrue(export.contains("first"))
        assertTrue(export.contains("second"))
        assertTrue(export.contains("third"))
    }
}
