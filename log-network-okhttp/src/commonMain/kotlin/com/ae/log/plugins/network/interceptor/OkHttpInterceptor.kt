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
 */
public class OkHttpInterceptor(
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
        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder
        val request = chain.request()

        // Fast path — plugin not installed, stay completely out of the way
        if (recorder == null) return chain.proceed(request)

        val id = recorder.newId()
        val startNs = System.nanoTime()

        // Read request body without consuming it
        val body = request.body
        val requestBody =
            if (body != null && !body.isOneShot()) {
                if (shouldCaptureBody(body.contentType()?.toString())) {
                    runCatching {
                        val buffer = Buffer()
                        body.writeTo(buffer)
                        if (buffer.size > maxRequestBodyBytes) {
                            buffer.readUtf8(maxRequestBodyBytes) + "\n… [truncated]"
                        } else {
                            buffer.readUtf8()
                        }
                    }.getOrNull()
                } else {
                    "<binary or unsupported, ${body.contentLength()} bytes>"
                }
            } else if (body != null) {
                "<one-shot body>"
            } else {
                null
            }

        recorder.startRequest(
            id = id,
            url = request.url.toString(),
            method = NetworkMethod.fromString(request.method),
            headers = request.headers.toMultiMap().redact(),
            body = requestBody,
        )

        return try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startNs) / 1_000_000

            val responseBody =
                if (shouldCaptureBody(response.body?.contentType()?.toString())) {
                    runCatching {
                        val bodyString = response.peekBody(maxResponseBodyBytes).string()
                        val contentLength = response.body?.contentLength() ?: -1L
                        if (contentLength > maxResponseBodyBytes ||
                            (contentLength == -1L && bodyString.length.toLong() >= maxResponseBodyBytes)
                        ) {
                            bodyString + "\n… [truncated]"
                        } else {
                            bodyString
                        }
                    }.getOrNull()
                } else {
                    val len = response.body?.contentLength() ?: -1
                    if (len > 0) "<binary or unsupported, $len bytes>" else "<binary or unsupported>"
                }

            recorder.logResponse(
                id = id,
                statusCode = response.code,
                headers = response.headers.toMultiMap().redact(),
                body = responseBody,
                durationMs = durationMs,
            )
            response
        } catch (t: Throwable) {
            recorder.logError(id, t.message ?: t::class.simpleName ?: "Unknown error")
            throw t
        }
    }
}
