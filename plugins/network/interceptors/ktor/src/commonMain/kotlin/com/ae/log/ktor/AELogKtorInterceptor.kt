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
) {
    public companion object Plugin : HttpClientPlugin<Config, AELogKtorInterceptor> {
        override val key: AttributeKey<AELogKtorInterceptor> = AttributeKey("AELogKtorInterceptor")

        private val RequestIdKey = AttributeKey<String>("AELogRequestId")
        private val StartTimeKey = AttributeKey<Long>("AELogStartTime")

        /** Headers excluded from the UI by default. Delegates to [InterceptorDefaults.DEFAULT_EXCLUDED]. */
        public var excludeHeaders: Set<String> = InterceptorDefaults.DEFAULT_EXCLUDED

        override fun prepare(block: Config.() -> Unit): AELogKtorInterceptor =
            AELogKtorInterceptor(maxBodyBytes = Config().apply(block).maxBodyBytes)

        override fun install(plugin: AELogKtorInterceptor, scope: HttpClient) {
            val clock = Clock.System
            val maxBytes = plugin.maxBodyBytes
            installOutgoingRequestPhase(scope, clock)
            installRequestBodyPhase(scope)
            installResponseMetadataPhase(scope, clock)
            installResponseBodyPhase(scope, maxBytes)
        }

        /** Resolves the recorder from the installed [NetworkPlugin], or `null`. */
        private fun recorder(): NetworkRecorder? =
            AELog.getPlugin<NetworkPlugin>()?.recorder

        // ── Phase 1: Outgoing request ─────────────────────────────────────

        private fun installOutgoingRequestPhase(scope: HttpClient, clock: Clock) {
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
                    headers = context.headers.build().entries()
                        .associate { it.key to it.value.joinToString(", ") }
                        .exclude(excludeHeaders),
                    body = extractBodyPreview(context.body),
                )

                try {
                    proceed()
                } catch (cause: Throwable) {
                    if (!isHttpResponseException(cause)) {
                        val msg = cause.message ?: cause::class.simpleName ?: "Unknown error"
                        recorder.logError(id, "${cause::class.simpleName}: ${msg.substringBefore(". Text:").trim()}")
                    }
                    throw cause
                }
            }
        }

        // ── Phase 1b: Request body (after serialization) ──────────────────

        private fun installRequestBodyPhase(scope: HttpClient) {
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) { content ->
                proceedWith(content)
                if (!AELog.isEnabled) return@intercept
                val recorder = recorder() ?: return@intercept
                val id = context.attributes.getOrNull(RequestIdKey) ?: return@intercept
                extractBodyPreview(context.body)?.let { recorder.updateRequestBody(id, it) }
            }
        }

        // ── Phase 2a: Response metadata ───────────────────────────────────

        private fun installResponseMetadataPhase(scope: HttpClient, clock: Clock) {
            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                val recorder = recorder()
                val id = response.call.attributes.getOrNull(RequestIdKey)
                if (!AELog.isEnabled || recorder == null || id == null) {
                    proceed()
                    return@intercept
                }

                val start = response.call.attributes.getOrNull(StartTimeKey)
                    ?: clock.now().toEpochMilliseconds()

                recorder.logResponse(
                    id = id,
                    statusCode = response.status.value,
                    headers = response.headers.entries()
                        .associate { it.key to it.value.joinToString(", ") }
                        .exclude(excludeHeaders),
                    durationMs = clock.now().toEpochMilliseconds() - start,
                )
                proceed()
            }
        }

        // ── Phase 2b: Response body ───────────────────────────────────────

        private fun installResponseBodyPhase(scope: HttpClient, maxBodyBytes: Int) {
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
                    val bytes = body.readBuffer().readByteArray()
                    val bodyText = if (bytes.size > maxBodyBytes) {
                        bytes.decodeToString(endIndex = maxBodyBytes).trim() + "\n… [truncated]"
                    } else {
                        bytes.decodeToString().trim().ifBlank { null }
                    }
                    recorder.updateResponseBody(id, bodyText)
                    proceedWith(HttpResponseContainer(info, ByteReadChannel(bytes)))
                }.onFailure { cause ->
                    recorder.logError(id, "body capture failed: ${cause.message}")
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
            if (body is TextContent) return body.text
            val name = body::class.simpleName ?: ""
            if ("MultiPart" in name || "FormData" in name) {
                val size = (body as? OutgoingContent)?.contentLength?.let { "$it bytes" } ?: "unknown size"
                return "<multipart/form-data: $size>"
            }
            return null
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
    }
}

public val KtorInterceptor: AELogKtorInterceptor.Plugin = AELogKtorInterceptor.Plugin
