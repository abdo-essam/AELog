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
 * @param excludeHeaders Headers that will be **completely removed** from the UI.
 *   Matching is case-insensitive. Defaults to [DEFAULT_EXCLUDED].
 */
public class OkHttpInterceptor(
    public val maxRequestBodyBytes: Long = 250_000L,
    public val maxResponseBodyBytes: Long = 250_000L,
    public val excludeHeaders: Set<String> = DEFAULT_EXCLUDED,
) : Interceptor {
    public companion object {
        /**
         * Headers excluded from the UI by default.
         *
         * Includes security-sensitive headers (Authorization, Cookie, Set-Cookie)
         * and verbose system headers that add noise without debugging value
         * (cache-control, date, strict-transport-security, etc.).
         *
         * Pass an empty set to show all headers, or override with your own list.
         */
        public val DEFAULT_EXCLUDED: Set<String> =
            setOf(
                // Security — never log raw tokens/cookies
                "Authorization",
                "Cookie",
                "Set-Cookie",
                "Proxy-Authorization",
                "X-Api-Key",
                // Auto-injected request headers — noise, set by the HTTP client
                "Accept",
                "Accept-Encoding",
                "Accept-Language",
                "Connection",
                "Content-Type",
                "Host",
                "User-Agent",
                // Noisy system response headers
                "Cache-Control",
                "Date",
                "Expires",
                "Pragma",
                "Server",
                "Strict-Transport-Security",
                "Transfer-Encoding",
                "Vary",
                "X-Content-Type-Options",
                "X-Frame-Options",
                "X-Powered-By",
                "X-XSS-Protection",
            )
    }

    /** Returns a new map with all [excludeHeaders] entries removed (case-insensitive). */
    private fun Map<String, String>.exclude(): Map<String, String> {
        if (excludeHeaders.isEmpty()) return this
        return filter { (key, _) ->
            excludeHeaders.none { it.equals(key, ignoreCase = true) }
        }
    }

    private fun okhttp3.Headers.toMultiMap(): Map<String, String> =
        names().associateWith { name -> values(name).joinToString(", ") }

    private fun shouldCaptureBody(contentType: String?): Boolean {
        // null content-type: try to read it anyway — it's almost always text/JSON in practice
        if (contentType == null) return true
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
                    }.getOrElse { "<body read error: ${it.message}>" }
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
            headers = request.headers.toMultiMap().exclude(),
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
                            "$bodyString\n… [truncated]"
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
                headers = response.headers.toMultiMap().exclude(),
                body = responseBody,
                durationMs = durationMs,
            )
            response
        } catch (t: Throwable) {
            val message = t.message ?: t::class.simpleName ?: "Unknown error"
            recorder.logError(id, "failed with exception: ${t::class.simpleName}: $message")
            throw t
        }
    }
}
