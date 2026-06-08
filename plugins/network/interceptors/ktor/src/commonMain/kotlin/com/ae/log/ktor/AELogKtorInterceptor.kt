package com.ae.log.ktor

import com.ae.log.AELog
import com.ae.log.network.NetworkPlugin
import com.ae.log.network.NetworkRecorder
import com.ae.log.network.config.InterceptorDefaults
import com.ae.log.network.config.InterceptorDefaults.exclude
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.time.Clock

/**
 * Ktor 3.x interceptor that records HTTP traffic to AELog.
 *
 * Ktor 3.x always installs a **SaveBody** plugin in `receivePipeline.Before`.
 * SaveBody drains the raw network channel and replaces it with a [SavedHttpResponse]
 * whose `rawContent` property creates a fresh [ByteReadChannel] from buffered bytes
 * **every time it is accessed** (Ktor KDoc: *“This property produces a new channel
 * every time it’s accessed”*). This makes it safe to read `rawContent` in later
 * receive-pipeline phases without exhausting the stream.
 *
 * Pipeline phases used:
 *  1. **Request/State** – records URL, method, and headers.
 *  2. **Request/Render** – captures the serialized request body.
 *  3. **Receive/State** – records status, headers, duration **and eagerly reads the
 *     response body** from `rawContent` (safe because SaveBody has already buffered
 *     it). Works for ALL responses including 4xx/5xx error responses.
 *  4. **Response/Receive** – re-injects the stored bytes as a fresh [ByteReadChannel]
 *     so the app can still call `bodyAsText()` / `body<T>()` normally.
 *
 * ## Usage
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(AELogKtorInterceptor) {
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

        /** Bytes buffered eagerly in receivePipeline so responsePipeline can re-inject them. */
        private val BufferedBodyKey = AttributeKey<ByteArray>("AELogBufferedBody")

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

                val recorder = recorder()
                val id = context.attributes.getOrNull(RequestIdKey)

                // When ContentNegotiation is NOT installed, setBody(String) or setBody(ByteArray)
                // passes raw primitives through the pipeline — they are never wrapped in OutgoingContent
                // at this phase. Handle them explicitly before attempting the OutgoingContent cast.
                if (recorder != null && id != null) {
                    when (content) {
                        is String -> {
                            recorder.updateRequestBody(id, content.trim().ifBlank { null })
                            proceedWith(content)
                            return@intercept
                        }
                        is ByteArray -> {
                            val text = runCatching { content.decodeToString().trim() }.getOrNull()
                            recorder.updateRequestBody(id, text?.ifBlank { null })
                            proceedWith(content)
                            return@intercept
                        }
                        else -> Unit // fall through to OutgoingContent handling below
                    }
                }

                val outgoing = content as? OutgoingContent
                val contentType = outgoing?.contentType?.toString() ?: context.headers["Content-Type"]
                val isCapturable = InterceptorDefaults.shouldCaptureBody(contentType, captureUnknown = true)

                if (!isCapturable) {
                    if (recorder != null && id != null) {
                        val size = outgoing?.contentLength ?: context.headers["Content-Length"]?.toLongOrNull()
                        val sizeStr = size?.let { "$it bytes" } ?: "unknown size"
                        val typeStr = contentType ?: "unknown content type"
                        val preview = "<$typeStr: $sizeStr>"
                        recorder.updateRequestBody(id, preview)
                    }
                    proceedWith(content)
                    return@intercept
                }

                // For WriteChannelContent (e.g. JSON serialized by ContentNegotiation),
                // we must drain the channel into a byte array, log it, then re-emit as
                // ByteArrayContent so the actual HTTP request body is preserved.
                if (outgoing is OutgoingContent.WriteChannelContent && recorder != null && id != null) {
                    val buffer = ByteChannel(autoFlush = true)
                    val bytes =
                        coroutineScope {
                            launch {
                                try {
                                    outgoing.writeTo(buffer)
                                } finally {
                                    buffer.flushAndClose()
                                }
                            }
                            buffer.toByteArray()
                        }
                    val bodyText = bytes.decodeToString().trim().ifBlank { null }
                    recorder.updateRequestBody(id, bodyText)
                    val replacement =
                        ByteArrayContent(
                            bytes = bytes,
                            contentType = outgoing.contentType,
                        )
                    proceedWith(replacement)
                    return@intercept
                }

                // For non-stream content (ByteArrayContent, TextContent, etc.)
                if (recorder != null && id != null) {
                    extractBodyPreview(content)?.let { recorder.updateRequestBody(id, it) }
                }

                proceedWith(content)
            }
        }

        // ── Response loggers ────────────────────────────────────────────────

        private fun installResponseLoggers(
            scope: HttpClient,
            clock: Clock,
            excludeHeaders: Set<String>,
            maxBodyBytes: Int,
        ) {
            // Phase 1 – Metadata + body capture in receivePipeline.State.
            //
            // By the time .State runs, Ktor's built-in SaveBody plugin (runs in
            // receivePipeline.Before) has already:
            //   • drained the raw network channel
            //   • replaced the response subject with a SavedHttpResponse
            //
            // SavedHttpResponse.rawContent returns a FRESH ByteReadChannel from
            // in-memory bytes on every access (Ktor KDoc guarantees this), so reading
            // it here does NOT exhaust the stream and does NOT interfere with the app
            // or with expectSuccess reading it later.
            //
            // This is the ONLY phase that reliably fires for ALL responses
            // (including 4xx/5xx) before any exception is thrown.
            //
            // @OptIn(InternalAPI::class) is required because rawContent is marked
            // @InternalAPI. Ktor's own SaveBody and SavedCall.save() also use @OptIn
            // for the same property – this is the accepted pattern for deep integration.
            @OptIn(InternalAPI::class)
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

                // Eagerly capture the body.
                // rawContent is safe to read here because SaveBody has already
                // buffered the bytes; each access creates a fresh channel.
                val contentType = response.headers["Content-Type"]
                if (InterceptorDefaults.shouldCaptureBody(contentType, captureUnknown = false)) {
                    runCatching {
                        val bytes = response.rawContent.toByteArray()
                        response.call.attributes.put(BufferedBodyKey, bytes)
                        val bodyText =
                            if (bytes.size > maxBodyBytes) {
                                bytes.decodeToString(endIndex = maxBodyBytes).trim() + "\n… [truncated]"
                            } else {
                                bytes.decodeToString().trim().ifBlank { null }
                            }
                        recorder.updateResponseBody(id, bodyText)
                    }.onFailure {
                        // rawContent read failed (e.g. truly streaming response) — skip.
                    }
                }

                proceed()
            }

            // Phase 2 – Re-inject bytes in responsePipeline.Receive.
            //
            // When the app (or expectSuccess) calls bodyAsText() / body<T>(), Ktor
            // invokes responsePipeline with the raw ByteReadChannel as subject. We
            // replace it with a fresh channel from our already-buffered bytes so the
            // caller gets a readable stream.
            //
            // If buffering in Phase 1 failed (BufferedBodyKey absent), we fall back to
            // reading and capturing the body here.
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
                    val buffered = context.attributes.getOrNull(BufferedBodyKey)
                    val bytes = buffered ?: body.toByteArray()

                    if (buffered == null) {
                        // Fallback: body wasn’t captured in receivePipeline — capture now.
                        context.attributes.put(BufferedBodyKey, bytes)
                        val bodyText =
                            if (bytes.size > maxBodyBytes) {
                                bytes.decodeToString(endIndex = maxBodyBytes).trim() + "\n… [truncated]"
                            } else {
                                bytes.decodeToString().trim().ifBlank { null }
                            }
                        recorder.updateResponseBody(id, bodyText)
                    }

                    // Always re-wrap so the caller gets a readable channel.
                    proceedWith(HttpResponseContainer(info, ByteReadChannel(bytes)))
                }.onFailure { cause ->
                    recorder.logError(id, "Could not decode response body: ${cause.message}")
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
                is OutgoingContent.WriteChannelContent,
                -> "<stream: ${body.contentLength ?: "unknown"} bytes>"
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
