package com.ae.log.plugins.network.interceptor

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.model.NetworkMethod
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * OkHttp [Interceptor] that automatically records every request/response pair
 * into the [NetworkPlugin] viewer — zero boilerplate, no ID management.
 *
 * ## Setup
 *
 * Add the dependency:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:log-network-okhttp:<version>")
 * ```
 *
 * Install the interceptor — one line:
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(AELogOkHttpInterceptor())
 *     .build()
 * ```
 *
 * Prefer [okhttp3.OkHttpClient.Builder.addNetworkInterceptor] over
 * [okhttp3.OkHttpClient.Builder.addInterceptor] to also capture cache hits
 * and see redirected / final network-level response bodies.
 *
 * ## What is captured
 *
 * | Field | Source |
 * |-------|--------|
 * | URL | `request.url` |
 * | Method | `request.method` |
 * | Request headers | `request.headers` |
 * | Request body | buffered peek — safe, non-consuming |
 * | Status code | `response.code` |
 * | Response headers | `response.headers` |
 * | Response body | `peekBody()` up to [MAX_BODY_BYTES] |
 * | Duration | wall-clock delta (request → first byte of response) |
 * | Error | `IOException.message` on connection failures |
 *
 * ## Silent no-op
 *
 * If [NetworkPlugin] is not installed or [AELog.init] has not been called,
 * [intercept] delegates straight to `chain.proceed()` with zero overhead.
 */
public class AELogOkHttpInterceptor(
    public val maxRequestBodyBytes: Long = 250_000L,
    public val maxResponseBodyBytes: Long = 250_000L,
    public val redactHeaders: Set<String> = DEFAULT_REDACTED,
) : Interceptor {
    public companion object {
        public val DEFAULT_REDACTED: Set<String> =
            setOf(
                "Authorization",
                "Cookie",
                "Set-Cookie",
                "Proxy-Authorization",
                "X-Api-Key",
            )
    }

    private fun Map<String, String>.redact(): Map<String, String> {
        if (redactHeaders.isEmpty()) return this
        return mapValues { (key, value) ->
            if (redactHeaders.any { it.equals(key, ignoreCase = true) }) "***" else value
        }
    }

    private fun okhttp3.Headers.toMultiMap(): Map<String, String> =
        names().associateWith { name -> values(name).joinToString(", ") }

    private fun shouldCaptureBody(contentType: String?): Boolean {
        if (contentType == null) return false
        return contentType.startsWith("text/", ignoreCase = true) ||
            contentType.contains("json", ignoreCase = true) ||
            contentType.contains("xml", ignoreCase = true) ||
            contentType.contains("form-urlencoded", ignoreCase = true)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!AELog.isEnabled) return chain.proceed(chain.request())
        val recorder = AELog.plugin<NetworkPlugin>()?.recorder
        val request = chain.request()

        // Fast path — plugin not installed, stay completely out of the way
        if (recorder == null) return chain.proceed(request)

        val id = recorder.newId()
        val startNs = System.nanoTime()

        // Read request body without consuming it (OkHttp bodies are single-read streams)
        val body = request.body
        val requestBody =
            if (body != null && !body.isOneShot()) {
                if (shouldCaptureBody(body.contentType()?.toString())) {
                    runCatching {
                        val buffer = Buffer()
                        body.writeTo(buffer)
                        buffer.readUtf8()
                    }.getOrNull()
                } else {
                    "<binary or unsupported, ${body.contentLength()} bytes>"
                }
            } else if (body != null) {
                "<one-shot body>"
            } else {
                null
            }

        recorder.request(
            id = id,
            url = request.url.toString(),
            method = NetworkMethod.fromString(request.method),
            rawMethod = request.method,
            headers = request.headers.toMultiMap().redact(),
            body = requestBody,
        )

        return try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startNs) / 1_000_000

            // peekBody() clones the internal source — the live response stream is NOT consumed
            val responseBody =
                if (shouldCaptureBody(response.body?.contentType()?.toString())) {
                    runCatching {
                        response.peekBody(maxResponseBodyBytes).string()
                    }.getOrNull()
                } else {
                    val len = response.body?.contentLength() ?: -1
                    if (len > 0) "<binary or unsupported, $len bytes>" else "<binary or unsupported>"
                }

            recorder.response(
                id = id,
                statusCode = response.code,
                headers = response.headers.toMultiMap().redact(),
                body = responseBody,
                durationMs = durationMs,
            )
            response
        } catch (t: Throwable) {
            recorder.error(id, t.message ?: t::class.simpleName ?: "Unknown error")
            throw t
        }
    }
}
