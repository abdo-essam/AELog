package com.ae.log.okhttp

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.interceptors.InterceptorDefaults
import com.ae.log.plugins.network.interceptors.InterceptorDefaults.exclude
import com.ae.log.plugins.network.model.NetworkMethod
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer

/**
 * OkHttp [Interceptor] that automatically records every request/response pair
 * into the [NetworkPlugin] viewer — zero boilerplate, no ID management.
 *
 * @param excludeHeaders Headers that will be **completely removed** from the UI.
 *   Matching is case-insensitive. Defaults to [InterceptorDefaults.DEFAULT_EXCLUDED].
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
         * Delegates to [InterceptorDefaults.DEFAULT_EXCLUDED] — both interceptors
         * stay in sync automatically.
         *
         * Pass an empty set to show all headers, or override with your own list.
         */
        public val DEFAULT_EXCLUDED: Set<String> = InterceptorDefaults.DEFAULT_EXCLUDED
    }

    private fun okhttp3.Headers.toMultiMap(): Map<String, String> =
        names().associateWith { name -> values(name).joinToString(", ") }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!AELog.isEnabled) return chain.proceed(chain.request())
        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder
        val request = chain.request()

        if (recorder == null) return chain.proceed(request)

        val id = recorder.newId()
        val startNs = System.nanoTime()

        val body = request.body
        val requestBody =
            when {
                body != null && !body.isOneShot() -> {
                    if (InterceptorDefaults.shouldCaptureBody(body.contentType()?.toString(), captureUnknown = true)) {
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
                }
                body != null -> "<one-shot body>"
                else -> null
            }

        recorder.startRequest(
            id = id,
            url = request.url.toString(),
            method = NetworkMethod.fromString(request.method),
            headers = request.headers.toMultiMap().exclude(excludeHeaders),
            body = requestBody,
        )

        return try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startNs) / 1_000_000

            val responseBody =
                if (InterceptorDefaults.shouldCaptureBody(
                        response.body.contentType()?.toString(),
                        captureUnknown = true,
                    )
                ) {
                    runCatching {
                        val bodyString = response.peekBody(maxResponseBodyBytes).string()
                        val contentLength = response.body.contentLength()
                        if (contentLength > maxResponseBodyBytes ||
                            (contentLength == -1L && bodyString.length.toLong() >= maxResponseBodyBytes)
                        ) {
                            "$bodyString\n… [truncated]"
                        } else {
                            bodyString
                        }
                    }.getOrNull()
                } else {
                    val len = response.body.contentLength()
                    if (len > 0) "<binary or unsupported, $len bytes>" else "<binary or unsupported>"
                }

            recorder.logResponse(
                id = id,
                statusCode = response.code,
                headers = response.headers.toMultiMap().exclude(excludeHeaders),
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
