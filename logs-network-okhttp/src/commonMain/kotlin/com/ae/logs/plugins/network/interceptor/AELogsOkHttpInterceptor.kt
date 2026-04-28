package com.ae.logs.plugins.network.interceptor

import com.ae.logs.AELogs
import com.ae.logs.plugins.network.NetworkPlugin
import com.ae.logs.plugins.network.model.NetworkMethod
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.IOException

/**
 * OkHttp [Interceptor] that automatically records every request/response pair
 * into the [NetworkPlugin] viewer — zero boilerplate, no ID management.
 *
 * ## Setup
 *
 * Add the dependency:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:logs-network-okhttp:<version>")
 * ```
 *
 * Install the interceptor — one line:
 * ```kotlin
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(AELogsOkHttpInterceptor())
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
 * If [NetworkPlugin] is not installed or [AELogs.init] has not been called,
 * [intercept] delegates straight to `chain.proceed()` with zero overhead.
 */
public class AELogsOkHttpInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val api = AELogs.plugin<NetworkPlugin>()?.api
        val request = chain.request()

        // Fast path — plugin not installed, stay completely out of the way
        if (api == null) return chain.proceed(request)

        val id = api.newId()
        val startMs = System.currentTimeMillis()

        // Read request body without consuming it (OkHttp bodies are single-read streams)
        val requestBody = runCatching {
            request.body?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            }
        }.getOrNull()

        api.request(
            id = id,
            url = request.url.toString(),
            method = NetworkMethod.fromString(request.method),
            headers = request.headers.toMap(),
            body = requestBody,
        )

        return try {
            val response = chain.proceed(request)
            val durationMs = System.currentTimeMillis() - startMs

            // peekBody() clones the internal source — the live response stream is NOT consumed
            val responseBody = runCatching {
                response.peekBody(MAX_BODY_BYTES).string()
            }.getOrNull()

            api.response(
                id = id,
                statusCode = response.code,
                headers = response.headers.toMap(),
                body = responseBody,
                durationMs = durationMs,
            )
            response
        } catch (e: IOException) {
            api.error(id, e.message ?: "Connection failed")
            throw e
        }
    }

    public companion object {
        /**
         * Maximum response body bytes captured by [okhttp3.Response.peekBody].
         * Bodies larger than this are truncated at the byte boundary.
         *
         * Default: **250 KB** — large enough for typical REST payloads without
         * risking OOM on large binary responses.
         */
        public const val MAX_BODY_BYTES: Long = 250_000L
    }
}
