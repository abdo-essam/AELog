package com.ae.log.ktor

import com.ae.log.AELog
import com.ae.log.network.NetworkPlugin
import com.ae.log.network.NetworkRecorder
import com.ae.log.network.interceptors.InterceptorDefaults
import com.ae.log.network.interceptors.InterceptorDefaults.exclude
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import io.ktor.util.*
import io.ktor.utils.io.*
import kotlinx.io.readByteArray
import kotlin.time.Clock

/**
 * Ktor 3.x interceptor that records HTTP traffic to AELog.
 *
 * Uses a two-phase approach:
 *  1. **Request pipeline** – records URL, method, headers, and body.
 *  2. **Receive pipeline** – records status, response headers, and duration.
 *  3. **Response pipeline** – patches the body once the app reads it,
 *     without consuming it (bytes are re-injected via a fresh [ByteReadChannel]).
 *
 * ## Usage
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(KtorInterceptor) {
 *         maxBodyBytes = 100_000  // optional — defaults to 250 KB
 *     }
 * }
 * ```
 */
public class AELogKtorInterceptor internal constructor(
    private val maxBodyBytes: Int,
    private val excludeHeaders: Set<String>,
) {
    public companion object Plugin : HttpClientPlugin<Config, AELogKtorInterceptor> {
        override val key: AttributeKey<AELogKtorInterceptor> = AttributeKey("AELogKtorInterceptor")

        private val RequestIdKey = AttributeKey<String>("AELogRequestId")
        private val StartTimeKey = AttributeKey<Long>("AELogStartTime")

        override fun prepare(block: Config.() -> Unit): AELogKtorInterceptor {
            val config = Config().apply(block)
            return AELogKtorInterceptor(config.maxBodyBytes, config.excludeHeaders)
        }

        override fun install(
            plugin: AELogKtorInterceptor,
            scope: HttpClient,
        ) {
            val clock = Clock.System
            installRequestLoggers(scope, clock, plugin.excludeHeaders)
            installResponseLoggers(scope, clock, plugin.excludeHeaders, plugin.maxBodyBytes)
        }

        /** Resolves the recorder from the installed [NetworkPlugin], or `null`. */
        private fun recorder(): NetworkRecorder? = AELog.getPlugin<NetworkPlugin>()?.recorder

        private fun installRequestLoggers(
            scope: HttpClient,
            clock: Clock,
            excludeHeaders: Set<String>,
        ) {
            // 1. Record metadata (URL, Method, Headers) early in the pipeline
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                if (!AELog.isEnabled) return@intercept
                val recorder = recorder() ?: return@intercept

                val id = recorder.newId()
                context.attributes.put(RequestIdKey, id)
                context.attributes.put(StartTimeKey, clock.now().toEpochMilliseconds())

                recorder.logRequest(
                    id = id,
                    url = context.url.buildString(),
                    method = context.method.value,
                    headers =
                        context.headers
                            .build()
                            .entries()
                            .associate { it.key to it.value.joinToString(", ") }
                            .exclude(excludeHeaders),
                    body = null, // Body is captured after serialization
                )

                try {
                    proceed()
                } catch (cause: Throwable) {
                    if (!isHttpResponseException(cause)) {
                        val msg = cause.message ?: cause::class.simpleName ?: "Unknown error"
                        recorder.logError(id, "${cause::class.simpleName}: $msg")
                    }
                    throw cause
                }
            }

            // 2. Capture the actual serialized body after Ktor formats it
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) { content ->
                if (!AELog.isEnabled) {
                    proceedWith(content)
                    return@intercept
                }
                proceedWith(content)
                val recorder = recorder() ?: return@intercept
                val id = context.attributes.getOrNull(RequestIdKey) ?: return@intercept
                extractBodyPreview(context.body)?.let { recorder.updateRequestBody(id, it) }
            }
        }

        private fun installResponseLoggers(
            scope: HttpClient,
            clock: Clock,
            excludeHeaders: Set<String>,
            maxBodyBytes: Int,
        ) {
            // 1. Record metadata (Status Code, Duration, Headers)
            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                val recorder = recorder()
                val id = response.call.attributes.getOrNull(RequestIdKey)
                if (!AELog.isEnabled || recorder == null || id == null) {
                    proceed()
                    return@intercept
                }

                val start =
                    response.call.attributes.getOrNull(StartTimeKey)
                        ?: clock.now().toEpochMilliseconds()

                recorder.logResponse(
                    id = id,
                    statusCode = response.status.value,
                    headers =
                        response.headers
                            .entries()
                            .associate { it.key to it.value.joinToString(", ") }
                            .exclude(excludeHeaders),
                    durationMs = clock.now().toEpochMilliseconds() - start,
                )
                proceed()
            }

            // 2. Read and duplicate the response body stream for logging
            scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
                val recorder = recorder()
                val id = context.attributes.getOrNull(RequestIdKey)
                if (!AELog.isEnabled || body !is ByteReadChannel || recorder == null || id == null) {
                    proceed()
                    return@intercept
                }

                val contentType = context.response.headers["Content-Type"]
                if (!InterceptorDefaults.shouldCaptureBody(contentType, captureUnknown = false)) {
                    proceed()
                    return@intercept
                }

                runCatching {
                    val bytes = body.toByteArray()
                    val bodyText =
                        if (bytes.size > maxBodyBytes) {
                            bytes.decodeToString(endIndex = maxBodyBytes).trim() + "\n… [truncated]"
                        } else {
                            bytes.decodeToString().trim().ifBlank { null }
                        }
                    recorder.updateResponseBody(id, bodyText)
                    proceedWith(HttpResponseContainer(info, ByteReadChannel(bytes)))
                }.onFailure { cause ->
                    recorder.logError(id, "Could not decode response body (possibly binary): ${cause.message}")
                    proceed()
                }
            }
        }

        // ── Helpers ───────────────────────────────────────────────────────

        /**
         * Ktor wraps HTTP errors in ResponseException / ClientRequestException /
         * ServerResponseException. These are *expected* HTTP responses (4xx/5xx),
         * not connection failures — we already record them via the receive pipeline.
         */
        private fun isHttpResponseException(cause: Throwable): Boolean =
            cause::class.simpleName?.let {
                "ResponseException" in it || "ClientRequestException" in it || "ServerResponseException" in it
            } ?: false

        private fun extractBodyPreview(body: Any): String? {
            if (body !is OutgoingContent) return null

            val contentType = body.contentType?.toString() ?: ""
            if (contentType.contains("multipart", ignoreCase = true) ||
                contentType.contains("form-data", ignoreCase = true)
            ) {
                val size = body.contentLength?.let { "$it bytes" } ?: "unknown size"
                return "<$contentType: $size>"
            }

            return when (body) {
                is OutgoingContent.ByteArrayContent -> runCatching { body.bytes().decodeToString() }.getOrNull()
                is OutgoingContent.ReadChannelContent,
                is OutgoingContent.WriteChannelContent -> "<stream: ${body.contentLength ?: "unknown"} bytes>"
                else -> null
            }
        }
    }

    /** Configuration block for [AELogKtorInterceptor]. */
    public class Config {
        /**
         * Maximum response body size to capture, in bytes.
         *
         * Bodies larger than this are truncated with a `… [truncated]` suffix.
         * Defaults to [InterceptorDefaults.DEFAULT_MAX_BODY_BYTES] (250 KB).
         */
        public var maxBodyBytes: Int = InterceptorDefaults.DEFAULT_MAX_BODY_BYTES.toInt()

        /** Headers excluded from the UI by default. Delegates to [InterceptorDefaults.DEFAULT_EXCLUDED]. */
        public var excludeHeaders: Set<String> = InterceptorDefaults.DEFAULT_EXCLUDED
    }
}

public val KtorInterceptor: AELogKtorInterceptor.Plugin = AELogKtorInterceptor.Plugin
