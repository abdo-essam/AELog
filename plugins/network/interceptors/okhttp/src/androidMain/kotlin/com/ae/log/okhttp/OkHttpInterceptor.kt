package com.ae.log.okhttp

import com.ae.log.AELog
import com.ae.log.network.NetworkPlugin
import com.ae.log.network.interceptors.InterceptorDefaults
import com.ae.log.network.interceptors.InterceptorDefaults.exclude
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer

/**
 * OkHttp [Interceptor] that automatically records every request/response pair
 * into the [NetworkPlugin] viewer — zero boilerplate, no ID management.
 *
 * @param excludeHeaders Headers to redact from the UI (case-insensitive).
 *   Defaults to [InterceptorDefaults.DEFAULT_EXCLUDED].
 */
public class OkHttpInterceptor(
    public val maxRequestBodyBytes: Long = InterceptorDefaults.DEFAULT_MAX_BODY_BYTES,
    public val maxResponseBodyBytes: Long = InterceptorDefaults.DEFAULT_MAX_BODY_BYTES,
    public val excludeHeaders: Set<String> = InterceptorDefaults.DEFAULT_EXCLUDED,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!AELog.isEnabled) return chain.proceed(request)
        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return chain.proceed(request)

        val id = recorder.newId()
        val startNs = System.nanoTime()

        recorder.logRequest(
            id = id,
            url = request.url.toString(),
            method = request.method,
            headers = request.headers.toMap().exclude(excludeHeaders),
            body = captureRequestBody(request),
        )

        return try {
            val response = chain.proceed(request)
            val durationMs = (System.nanoTime() - startNs) / 1_000_000

            recorder.logResponse(
                id = id,
                statusCode = response.code,
                headers = response.headers.toMap().exclude(excludeHeaders),
                body = captureResponseBody(response),
                durationMs = durationMs,
            )
            response
        } catch (t: Throwable) {
            val msg = t.message ?: t::class.simpleName ?: "Unknown error"
            recorder.logError(id, "${t::class.simpleName}: $msg")
            throw t
        }
    }

    // ── Body capture ──────────────────────────────────────────────────────

    private fun captureRequestBody(request: Request): String? {
        val body = request.body ?: return null
        if (body.isOneShot()) return "<one-shot body>"

        val contentType = body.contentType()?.toString()
        if (!InterceptorDefaults.shouldCaptureBody(contentType, captureUnknown = true)) {
            return "<binary or unsupported, ${body.contentLength()} bytes>"
        }

        return runCatching {
            val buffer = Buffer()
            body.writeTo(buffer)
            if (buffer.size > maxRequestBodyBytes) {
                buffer.readUtf8(maxRequestBodyBytes) + "\n… [truncated]"
            } else {
                buffer.readUtf8()
            }
        }.getOrElse { "<body read error: ${it.message}>" }
    }

    private fun captureResponseBody(response: Response): String? {
        val contentType = response.body.contentType()?.toString()
        if (!InterceptorDefaults.shouldCaptureBody(contentType, captureUnknown = true)) {
            val len = response.body.contentLength()
            return if (len > 0) "<binary or unsupported, $len bytes>" else "<binary or unsupported>"
        }

        return runCatching {
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
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private fun okhttp3.Headers.toMap(): Map<String, String> =
        names().associateWith { name -> values(name).joinToString(", ") }
}
