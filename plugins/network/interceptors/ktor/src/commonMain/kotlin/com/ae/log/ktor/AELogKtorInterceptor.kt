package com.ae.log.ktor

import com.ae.log.AELog
import com.ae.log.network.NetworkPlugin
import com.ae.log.network.interceptors.InterceptorDefaults
import com.ae.log.network.interceptors.InterceptorDefaults.exclude
import com.ae.log.network.model.NetworkMethod
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
    public companion object Plugin : HttpClientPlugin<AELogKtorInterceptor.Config, AELogKtorInterceptor> {
        override val key: AttributeKey<AELogKtorInterceptor> = AttributeKey("AELogKtorInterceptor")

        private val RequestIdKey = AttributeKey<String>("AELogRequestId")
        private val StartTimeKey = AttributeKey<Long>("AELogStartTime")

        /** Headers excluded from the UI by default. Delegates to [InterceptorDefaults.DEFAULT_EXCLUDED]. */
        public var excludeHeaders: Set<String> = InterceptorDefaults.DEFAULT_EXCLUDED

        override fun prepare(block: Config.() -> Unit): AELogKtorInterceptor =
            AELogKtorInterceptor(
                maxBodyBytes = Config().apply(block).maxBodyBytes,
            )

        override fun install(plugin: AELogKtorInterceptor, scope: HttpClient) {
            val clock = Clock.System
            val maxBodyBytes = plugin.maxBodyBytes
            installOutgoingRequestPhase(scope, clock)
            installRequestBodyPhase(scope)
            installResponseMetadataPhase(scope, clock)
            installResponseBodyPhase(scope, maxBodyBytes)
        }

        // ── Phase 1: Outgoing requests ────────────────────────────────────

        private fun installOutgoingRequestPhase(scope: HttpClient, clock: Clock) {
            scope.requestPipeline.intercept(HttpRequestPipeline.State) {
                if (!AELog.isEnabled) return@intercept
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept

                val id = recorder.newId()
                context.attributes.put(RequestIdKey, id)
                context.attributes.put(StartTimeKey, clock.now().toEpochMilliseconds())

                val headersMap = context.headers.build().entries()
                    .associate { it.key to it.value.joinToString(", ") }

                recorder.startRequest(
                    id = id,
                    url = context.url.buildString(),
                    method = NetworkMethod.fromString(context.method.value),
                    headers = headersMap.exclude(excludeHeaders),
                    body = extractBodyPreview(context.body),
                )

                // Catch send-level failures (UnknownHostException, timeout, etc.)
                try {
                    proceed()
                } catch (cause: Throwable) {
                    val isHttpError = cause::class.simpleName?.let {
                        it.contains("ResponseException") ||
                            it.contains("ClientRequestException") ||
                            it.contains("ServerResponseException")
                    } ?: false
                    if (!isHttpError) {
                        val raw = cause.message ?: cause::class.simpleName ?: "Unknown error"
                        recorder.logError(id, "${cause::class.simpleName}: ${raw.substringBefore(". Text:").trim()}")
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
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept
                val id = context.attributes.getOrNull(RequestIdKey) ?: return@intercept
                val bodyText = extractBodyPreview(context.body)
                if (bodyText != null) recorder.updateRequestBody(id, bodyText)
            }
        }

        // ── Phase 2a: Response metadata (status, headers, duration) ───────

        private fun installResponseMetadataPhase(scope: HttpClient, clock: Clock) {
            scope.receivePipeline.intercept(HttpReceivePipeline.State) { response ->
                if (!AELog.isEnabled) { proceed(); return@intercept }
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder
                    ?: run { proceed(); return@intercept }
                val id = response.call.attributes.getOrNull(RequestIdKey)
                    ?: run { proceed(); return@intercept }
                val start = response.call.attributes.getOrNull(StartTimeKey)
                    ?: clock.now().toEpochMilliseconds()

                recorder.logResponse(
                    id = id,
                    statusCode = response.status.value,
                    headers = response.headers.entries()
                        .associate { it.key to it.value.joinToString(", ") }
                        .exclude(excludeHeaders),
                    body = null,
                    durationMs = clock.now().toEpochMilliseconds() - start,
                )
                proceed()
            }
        }

        // ── Phase 2b: Response body — tapped when the app reads it ────────

        private fun installResponseBodyPhase(scope: HttpClient, maxBodyBytes: Int) {
            scope.responsePipeline.intercept(HttpResponsePipeline.Receive) { (info, body) ->
                if (!AELog.isEnabled || body !is ByteReadChannel) { proceed(); return@intercept }
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder
                    ?: run { proceed(); return@intercept }
                val id = context.attributes.getOrNull(RequestIdKey)
                    ?: run { proceed(); return@intercept }

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

        // ── Shared utilities ──────────────────────────────────────────────

        private fun extractBodyPreview(body: Any): String? {
            if (body is TextContent) return body.text
            val name = body::class.simpleName ?: ""
            if (name.contains("MultiPart", ignoreCase = true) || name.contains("FormData", ignoreCase = true)) {
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
