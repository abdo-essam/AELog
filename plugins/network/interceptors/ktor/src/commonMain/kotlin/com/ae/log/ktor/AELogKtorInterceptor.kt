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
 */
public class AELogKtorInterceptor internal constructor() {
    public companion object Plugin : HttpClientPlugin<Unit, AELogKtorInterceptor> {
        override val key: AttributeKey<AELogKtorInterceptor> = AttributeKey("AELogKtorInterceptor")

        private val RequestIdKey = AttributeKey<String>("AELogRequestId")
        private val StartTimeKey = AttributeKey<Long>("AELogStartTime")

        /** Headers excluded from the UI by default. Delegates to [InterceptorDefaults.DEFAULT_EXCLUDED]. */
        public var excludeHeaders: Set<String> = InterceptorDefaults.DEFAULT_EXCLUDED

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
                    headers = headersMap.exclude(excludeHeaders),
                    body = extractBodyPreview(context.body),
                )

                // Catch send-level failures (UnknownHostException, timeout, etc.)
                try {
                    proceed()
                } catch (cause: Throwable) {
                    val isHttpError =
                        cause::class.simpleName?.let {
                            it.contains("ResponseException") ||
                                it.contains("ClientRequestException") ||
                                it.contains("ServerResponseException")
                        } ?: false
                    if (!isHttpError) {
                        val raw = cause.message ?: cause::class.simpleName ?: "Unknown error"
                        val clean = raw.substringBefore(". Text:").trim()
                        recorder.logError(id, "${cause::class.simpleName}: $clean")
                    }
                    throw cause
                }
            }

            // ── Phase 1b: Request Body (after serialization) ──────────────
            scope.requestPipeline.intercept(HttpRequestPipeline.Render) { content ->
                proceedWith(content)
                if (!AELog.isEnabled) return@intercept
                val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return@intercept
                val id = context.attributes.getOrNull(RequestIdKey) ?: return@intercept
                val bodyText = extractBodyPreview(context.body)
                if (bodyText != null) recorder.updateRequestBody(id, bodyText)
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
                val start = response.call.attributes.getOrNull(StartTimeKey) ?: clock.now().toEpochMilliseconds()

                recorder.logResponse(
                    id = id,
                    statusCode = response.status.value,
                    headers =
                        response.headers
                            .entries()
                            .associate { it.key to it.value.joinToString(", ") }
                            .exclude(excludeHeaders),
                    body = null,
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
                val contentType = context.response.headers["Content-Type"]

                if (InterceptorDefaults.shouldCaptureBody(contentType, captureUnknown = false)) {
                    runCatching {
                        val buffer = body.readBuffer()
                        val bytes = buffer.readByteArray()
                        recorder.updateResponseBody(id, bytes.decodeToString().trim().ifBlank { null })
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
    }
}

public val KtorInterceptor: AELogKtorInterceptor.Plugin = AELogKtorInterceptor.Plugin
