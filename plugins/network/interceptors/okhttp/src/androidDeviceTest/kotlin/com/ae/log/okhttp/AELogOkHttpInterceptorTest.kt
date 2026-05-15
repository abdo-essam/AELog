package com.ae.log.okhttp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.network.NetworkPlugin
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertTrue

/**
 * Instrumented tests for [AELogOkHttpInterceptor].
 *
 * Runs on a real Android device/emulator — all Android framework classes
 * (including android.util.Log used by OkHttp's Platform initializer) are
 * natively available. No stubs or Robolectric needed.
 */
@RunWith(AndroidJUnit4::class)
@OptIn(AELogTestApi::class)
class AELogOkHttpInterceptorTest {
    private lateinit var server: MockWebServer
    private lateinit var plugin: NetworkPlugin

    @Before
    fun setUp() {
        plugin = NetworkPlugin()
        AELog.init(plugin)
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.close()
        AELog.resetForTesting()
    }

    private fun client(interceptor: AELogOkHttpInterceptor = AELogOkHttpInterceptor()): OkHttpClient =
        OkHttpClient.Builder().addInterceptor(interceptor).build()

    private fun baseUrl(path: String = ""): String = server.url("/$path").toString()

    private fun enqueue(
        body: String = "{}",
        code: Int = 200,
        contentType: String = "application/json",
    ) = server.enqueue(
        MockResponse
            .Builder()
            .code(code)
            .body(body)
            .addHeader("Content-Type", contentType)
            .build(),
    )

    private fun export(): String = plugin.export()

    // ── Basic recording ───────────────────────────────────────────────────

    @Test
    fun records_GET_request_url_and_method() {
        enqueue()
        client().newCall(Request.Builder().url(baseUrl("users")).build()).execute().close()

        val export = export()
        assertTrue(export.contains("GET"))
        assertTrue(export.contains("users"))
    }

    @Test
    fun records_POST_request_with_body() {
        enqueue(body = """{"id":"1"}""", code = 201)
        val body = """{"name":"test"}""".toRequestBody("application/json".toMediaType())
        client()
            .newCall(
                Request
                    .Builder()
                    .url(baseUrl("data"))
                    .post(body)
                    .build(),
            ).execute()
            .close()

        val export = export()
        assertTrue(export.contains("POST"))
        assertTrue(export.contains("name"))
    }

    @Test
    fun records_response_status_code() {
        enqueue(code = 201)
        client().newCall(Request.Builder().url(baseUrl()).build()).execute().close()
        assertTrue(export().contains("201"))
    }

    @Test
    fun records_response_body() {
        enqueue(body = """{"users":["a","b"]}""")
        client().newCall(Request.Builder().url(baseUrl()).build()).execute().close()
        assertTrue(export().contains("users"))
    }

    @Test
    fun records_request_duration() {
        enqueue()
        client().newCall(Request.Builder().url(baseUrl()).build()).execute().close()
        assertTrue(export().contains("ms"))
    }

    // ── isEnabled gate ────────────────────────────────────────────────────

    @Test
    fun does_not_record_when_AELog_is_disabled() {
        AELog.isEnabled = false
        enqueue()
        client().newCall(Request.Builder().url(baseUrl()).build()).execute().close()
        assertTrue(export().isEmpty())
    }

    @Test
    fun resumes_recording_after_re_enabling() {
        AELog.isEnabled = false
        enqueue()
        client().newCall(Request.Builder().url(baseUrl("skip")).build()).execute().close()

        AELog.isEnabled = true
        enqueue()
        client().newCall(Request.Builder().url(baseUrl("capture")).build()).execute().close()

        val export = export()
        assertTrue(!export.contains("skip"))
        assertTrue(export.contains("capture"))
    }

    // ── Error handling ────────────────────────────────────────────────────

    @Test
    fun records_error_on_connection_failure() {
        server.close()
        val threw =
            try {
                client().newCall(Request.Builder().url(baseUrl()).build()).execute()
                false
            } catch (_: Throwable) {
                true
            }
        assertTrue(threw)
        val export = export()
        assertTrue(export.contains("Error") || export.contains("failed") || export.contains("refused"))
    }

    // ── Header exclusion ──────────────────────────────────────────────────

    @Test
    fun excludes_default_sensitive_headers() {
        enqueue()
        val request =
            Request
                .Builder()
                .url(baseUrl())
                .header("Authorization", "Bearer secret-token")
                .header("X-Custom", "visible-value")
                .build()
        client().newCall(request).execute().close()
        assertTrue(!export().contains("secret-token"))
    }

    @Test
    fun includes_headers_when_exclude_set_is_empty() {
        enqueue()
        val request =
            Request
                .Builder()
                .url(baseUrl())
                .header("Authorization", "Bearer visible-token")
                .build()
        client(AELogOkHttpInterceptor(excludeHeaders = emptySet())).newCall(request).execute().close()
        assertTrue(export().contains("Bearer visible-token"))
    }

    // ── Body truncation ───────────────────────────────────────────────────

    @Test
    fun truncates_large_request_bodies() {
        enqueue()
        val largeBody = "x".repeat(1000).toRequestBody("text/plain".toMediaType())
        client(AELogOkHttpInterceptor(maxRequestBodyBytes = 100))
            .newCall(
                Request
                    .Builder()
                    .url(baseUrl())
                    .post(largeBody)
                    .build(),
            ).execute()
            .close()
        assertTrue(export().contains("[truncated]"))
    }

    @Test
    fun truncates_large_response_bodies() {
        enqueue(body = "y".repeat(1000))
        client(AELogOkHttpInterceptor(maxResponseBodyBytes = 100))
            .newCall(Request.Builder().url(baseUrl()).build())
            .execute()
            .close()
        assertTrue(export().contains("[truncated]"))
    }

    // ── Binary content ────────────────────────────────────────────────────

    @Test
    fun marks_binary_content_instead_of_capturing_body() {
        enqueue(body = "fake-image-bytes", contentType = "image/png")
        client().newCall(Request.Builder().url(baseUrl()).build()).execute().close()
        val export = export()
        assertTrue(!export.contains("fake-image-bytes"))
        assertTrue(export.contains("binary or unsupported"))
    }

    // ── Multiple requests ─────────────────────────────────────────────────

    @Test
    fun records_multiple_requests_independently() {
        val c = client()
        repeat(3) { enqueue() }
        listOf("first", "second", "third").forEach { path ->
            c.newCall(Request.Builder().url(baseUrl(path)).build()).execute().close()
        }
        val export = export()
        assertTrue(export.contains("first"))
        assertTrue(export.contains("second"))
        assertTrue(export.contains("third"))
    }
}
