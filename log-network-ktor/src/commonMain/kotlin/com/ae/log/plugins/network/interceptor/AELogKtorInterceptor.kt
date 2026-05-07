@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.network.interceptor

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.model.NetworkMethod
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
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
                    headers = headersMap,
                    body = (context.body as? TextContent)?.text,
                )
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
                    headers = response.headers.entries().associate { it.key to it.value.joinToString(", ") },
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
                    // readBuffer() reads all bytes into a kotlinx.io.Buffer (Ktor 3.x public API)
                    val buffer = body.readBuffer()
                    val bytes = buffer.readByteArray()
                    val bodyText =
                        runCatching {
                            bytes.decodeToString().trim().ifBlank { null }
                        }.getOrNull()

                    recorder.updateResponseBody(id, bodyText)

                    // Re-inject fresh channel so the rest of the pipeline can decode the body
                    proceedWith(HttpResponseContainer(info, ByteReadChannel(bytes)))
                } else {
                    proceed()
                }
            }
        }

        private fun shouldCaptureBody(contentType: String): Boolean =
            contentType.contains("json", ignoreCase = true) ||
                contentType.startsWith("text/", ignoreCase = true) ||
                contentType.contains("xml", ignoreCase = true) ||
                contentType.contains("form-urlencoded", ignoreCase = true)
    }
}

public val KtorInterceptor: AELogKtorInterceptor.Plugin = AELogKtorInterceptor.Plugin
