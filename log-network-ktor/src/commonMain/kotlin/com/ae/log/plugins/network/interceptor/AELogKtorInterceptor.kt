package com.ae.log.plugins.network.interceptor

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.model.NetworkMethod
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
 *  1. [receivePipeline] – records status, headers, and duration immediately.
 *  2. [responsePipeline] – patches the body once the app reads it, without
 *     consuming it (bytes are re-injected via a fresh [ByteReadChannel]).
 *
 * No Ktor `@InternalAPI` required.
 */
public class AELogKtorInterceptor internal constructor() {
    public companion object Plugin : HttpClientPlugin<Unit, AELogKtorInterceptor> {
        override val key: AttributeKey<AELogKtorInterceptor> = AttributeKey("AELogKtorInterceptor")

        private val RequestIdKey = AttributeKey<String>("AELogRequestId")
        private val StartTimeKey = AttributeKey<Long>("AELogStartTime")

        /** Same default exclusion list as OkHttpInterceptor.DEFAULT_EXCLUDED. */
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

        /** The currently active exclusion set. Change before installing if needed. */
        public var excludeHeaders: Set<String> = DEFAULT_EXCLUDED

        private fun Map<String, String>.exclude(): Map<String, String> {
            if (excludeHeaders.isEmpty()) return this
            return filter { (key, _) ->
                excludeHeaders.none { it.equals(key, ignoreCase = true) }
            }
        }

        override fun prepare(block: Unit.() -> Unit): AELogKtorInterceptor = AELogKtorInterceptor()

        override fun install(
            plugin: AELogKtorInterceptor,
            scope: HttpClient,
        ) {
            val clock = Clock.System

            // ── Phase 1: Outgoing requests ────────────────────────────────
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                if (!AELog.isEnabled) return@intercept
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept

                val id = recorder.newId()
                context.attributes.put(RequestIdKey, id)
                context.attributes.put(StartTimeKey, clock.now().toEpochMilliseconds())

                val headersMap =
                    context.headers
                        .build()
                        .entries()
                        .associate { it.key to it.value.joinToString(", ") }

                recorder.startRequest(
                    id = id,
                    url = context.url.buildString(),
                    method = NetworkMethod.fromString(context.method.value),
                    headers = headersMap.exclude(),
                    body = extractBodyPreview(context.body),
                )

                // Catch send-level failures (UnknownHostException, timeout, etc.)
                // Note: 4xx/5xx ResponseExceptions are NOT recorded as errors — the status
                // code already captures that. Only true connection failures are logged here.
                try {
                    proceed()
                } catch (cause: Throwable) {
                    // Skip ResponseException (4xx/5xx) — status code is already recorded
                    val isHttpError = cause::class.simpleName
                        ?.contains("ResponseException") == true ||
                        cause::class.simpleName
                        ?.contains("ClientRequestException") == true ||
                        cause::class.simpleName
                        ?.contains("ServerResponseException") == true
                    if (!isHttpError) {
                        // Real connection failure: strip noise, keep the core message
                        val raw = cause.message ?: cause::class.simpleName ?: "Unknown error"
                        val clean = raw.substringBefore(". Text:").trim()
                        recorder.logError(id, "${cause::class.simpleName}: $clean")
                    }
                    throw cause
                }
            }

            // ── Phase 1b: Request Body (after serialization) ──────────────
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) { content ->
                proceedWith(content) // Let Ktor do its serialization first
                if (!AELog.isEnabled) return@intercept
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept

                val id = context.attributes.getOrNull(RequestIdKey) ?: return@intercept
                
                // After serialization, context.body is usually a TextContent (e.g. JSON string)
                // or a MultiPartFormDataContent.
                val bodyText = extractBodyPreview(context.body)
                if (bodyText != null) {
                    recorder.updateRequestBody(id, bodyText)
                }
            }

            // ── Phase 2a: Response metadata (status, headers, duration) ───
            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                if (!AELog.isEnabled) {
                    proceed()
                    return@intercept
                }
                val recorder =
                    AELog.getPlugin<NetworkPlugin>()?.recorder ?: run {
                        proceed()
                        return@intercept
                    }

                val id =
                    response.call.attributes.getOrNull(RequestIdKey) ?: run {
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
                            .exclude(),
                    body = null, // body patched in Phase 2b
                    durationMs = clock.now().toEpochMilliseconds() - start,
                )
                proceed()
            }

            // ── Phase 2b: Response body — intercept when app reads it ─────
            scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
                if (!AELog.isEnabled || body !is ByteReadChannel) {
                    proceed()
                    return@intercept
                }
                val recorder =
                    AELog.getPlugin<NetworkPlugin>()?.recorder ?: run {
                        proceed()
                        return@intercept
                    }

                val id =
                    context.attributes.getOrNull(RequestIdKey) ?: run {
                        proceed()
                        return@intercept
                    }
                val contentType = context.response.headers["Content-Type"] ?: ""

                if (shouldCaptureBody(contentType)) {
                    runCatching {
                        // readBuffer() reads all bytes into a kotlinx.io.Buffer (Ktor 3.x public API)
                        val buffer = body.readBuffer()
                        val bytes = buffer.readByteArray()
                        val bodyText =
                            bytes.decodeToString().trim().ifBlank { null }

                        recorder.updateResponseBody(id, bodyText)

                        // Re-inject fresh channel so the rest of the pipeline can decode the body
                        proceedWith(HttpResponseContainer(info, ByteReadChannel(bytes)))
                    }.onFailure { cause ->
                        recorder.logError(id, "body capture failed: ${cause.message}")
                        proceed()
                    }
                } else {
                    proceed()
                }
            }
        }

        private fun extractBodyPreview(body: Any): String? {
            if (body is TextContent) return body.text
            
            val name = body::class.simpleName ?: ""
            if (name.contains("MultiPart", ignoreCase = true) || name.contains("FormData", ignoreCase = true)) {
                val size = (body as? OutgoingContent)?.contentLength?.let { "$it bytes" } ?: "unknown size"
                return "<multipart/form-data: $size>"
            }
            return null
        }

        private fun shouldCaptureBody(contentType: String): Boolean =
            contentType.contains("json", ignoreCase = true) ||
                contentType.startsWith("text/", ignoreCase = true) ||
                contentType.contains("xml", ignoreCase = true) ||
                contentType.contains("form-urlencoded", ignoreCase = true)
    }
}

public val KtorInterceptor: AELogKtorInterceptor.Plugin = AELogKtorInterceptor.Plugin
