package com.ae.log.network.config

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InterceptorDefaultsTest {
    // ── Header exclusion ──────────────────────────────────────────────────

    @Test
    fun `exclude - removes exact matching headers`() {
        val headers = mapOf("Authorization" to "Bearer abc", "Content-Length" to "42")
        val result = with(InterceptorDefaults) { headers.exclude(setOf("Authorization")) }
        assertFalse(result.containsKey("Authorization"))
        assertTrue(result.containsKey("Content-Length"))
    }

    @Test
    fun `exclude - is case-insensitive`() {
        val headers = mapOf("authorization" to "Bearer abc", "X-Api-Key" to "secret")
        val result =
            with(InterceptorDefaults) {
                headers.exclude(setOf("Authorization", "x-api-key"))
            }
        assertTrue(result.isEmpty())
    }

    @Test
    fun `exclude - returns original map when exclusion set is empty`() {
        val headers = mapOf("Authorization" to "Bearer abc")
        val result = with(InterceptorDefaults) { headers.exclude(emptySet()) }
        assertEquals(headers, result)
    }

    @Test
    fun `DEFAULT_EXCLUDED - contains security-sensitive headers`() {
        val sensitive = listOf("Authorization", "Cookie", "Set-Cookie", "Proxy-Authorization", "X-Api-Key")
        sensitive.forEach { header ->
            assertTrue(
                InterceptorDefaults.DEFAULT_EXCLUDED.any { it.equals(header, ignoreCase = true) },
                "Expected DEFAULT_EXCLUDED to contain $header",
            )
        }
    }

    // ── Body capture decision ─────────────────────────────────────────────

    @Test
    fun `shouldCaptureBody - returns true for JSON content type`() {
        assertTrue(InterceptorDefaults.shouldCaptureBody("application/json"))
        assertTrue(InterceptorDefaults.shouldCaptureBody("application/json; charset=utf-8"))
    }

    @Test
    fun `shouldCaptureBody - returns true for text content types`() {
        assertTrue(InterceptorDefaults.shouldCaptureBody("text/plain"))
        assertTrue(InterceptorDefaults.shouldCaptureBody("text/html"))
        assertTrue(InterceptorDefaults.shouldCaptureBody("text/xml"))
    }

    @Test
    fun `shouldCaptureBody - returns true for form-urlencoded`() {
        assertTrue(InterceptorDefaults.shouldCaptureBody("application/x-www-form-urlencoded"))
    }

    @Test
    fun `shouldCaptureBody - returns false for binary content types`() {
        assertFalse(InterceptorDefaults.shouldCaptureBody("image/png"))
        assertFalse(InterceptorDefaults.shouldCaptureBody("application/octet-stream"))
        assertFalse(InterceptorDefaults.shouldCaptureBody("video/mp4"))
    }

    @Test
    fun `shouldCaptureBody - returns captureUnknown value when contentType is null`() {
        assertFalse(InterceptorDefaults.shouldCaptureBody(null, captureUnknown = false))
        assertTrue(InterceptorDefaults.shouldCaptureBody(null, captureUnknown = true))
    }

    @Test
    fun `DEFAULT_MAX_BODY_BYTES - is 250KB`() {
        assertEquals(250_000L, InterceptorDefaults.DEFAULT_MAX_BODY_BYTES)
    }
}
