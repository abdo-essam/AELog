@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.ae.log.plugins.network.interceptor

import com.ae.log.AELog
import com.ae.log.plugins.network.NetworkPlugin
import com.ae.log.plugins.network.model.NetworkMethod
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.TextContent

/**
 * Ktor [io.ktor.client.HttpClient] plugin that automatically records every
 * request/response pair into the [NetworkPlugin] viewer — zero boilerplate,
 * no ID management.
 *
 * ## Setup
 *
 * Add the dependency:
 * ```kotlin
 * // build.gradle.kts
 * implementation("io.github.abdo-essam:log-network-ktor:<version>")
 * ```
 *
 * Install the plugin — one line:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(AELogKtorInterceptor)
 * }
 * ```
 *
 * Every subsequent call is captured automatically.
 *
 * ## What is captured
 *
 * | Field | Source |
 * |-------|--------|
 * | URL | `request.url` |
 * | Method | `request.method` |
 * | Request headers | `request.headers` |
 * | Status code | `response.status.value` |
 * | Response headers | `response.headers` |
 * | Duration | monotonic clock delta (request → response) |
 *
 * Request and response **bodies** are not captured to avoid consuming streams.
 *
 * ## Silent no-op
 *
 * If [NetworkPlugin] is not installed or [AELog.init] has not been called,
 * every hook returns immediately — the real HTTP client is never affected.
 */

public class AELogKtorConfig {
    public var redactHeaders: Set<String> =
        setOf(
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "Proxy-Authorization",
            "X-Api-Key",
        )
    public var maxRequestBodyBytes: Long = 250_000L
    public var maxResponseBodyBytes: Long = 250_000L
    public var excludeUrls: List<Regex> = emptyList()
}

public val AELogKtorInterceptor: ClientPlugin<AELogKtorConfig> =
    createClientPlugin("AELogKtor", ::AELogKtorConfig) {
        val redactHeaders = pluginConfig.redactHeaders
        val excludeUrls = pluginConfig.excludeUrls

        fun Map<String, String>.redact(): Map<String, String> {
            if (redactHeaders.isEmpty()) return this
            return mapValues { (key, value) ->
                if (redactHeaders.any { it.equals(key, ignoreCase = true) }) "***" else value
            }
        }

        fun shouldCaptureBody(contentType: String?): Boolean {
            if (contentType == null) return false
            return contentType.startsWith("text/", ignoreCase = true) ||
                contentType.contains("json", ignoreCase = true) ||
                contentType.contains("xml", ignoreCase = true) ||
                contentType.contains("form-urlencoded", ignoreCase = true)
        }

        on(Send) { request ->
            if (!AELog.isEnabled) return@on proceed(request)
            val api = AELog.plugin<NetworkPlugin>()?.api ?: return@on proceed(request)

            val urlString = request.url.buildString()
            if (excludeUrls.any { it.matches(urlString) }) return@on proceed(request)

            val id = api.newId()
            val startMark =
                kotlin.time.TimeSource.Monotonic
                    .markNow()

            val contentType = request.headers["Content-Type"]
            val reqBody =
                if (shouldCaptureBody(contentType)) {
                    when (val content = request.body) {
                        is TextContent -> content.text
                        is ByteArrayContent -> content.bytes().decodeToString()
                        is String -> content
                        else -> "<streamed or unknown body>"
                    }
                } else {
                    val len = request.headers["Content-Length"]
                    if (len != null) "<binary or unsupported, $len bytes>" else "<binary or unsupported>"
                }

            api.request(
                id = id,
                url = request.url.buildString(),
                method = NetworkMethod.fromString(request.method.value),
                rawMethod = request.method.value,
                headers =
                    request.headers
                        .entries()
                        .associate { (key, values) -> key to values.joinToString(", ") }
                        .redact(),
                body = reqBody,
            )

            try {
                val response = proceed(request)
                val durationMs = startMark.elapsedNow().inWholeMilliseconds

                val resBody = null // To avoid consuming stream, use ResponseObserver plugin instead.

                api.response(
                    id = id,
                    statusCode = response.response.status.value,
                    headers =
                        response.response.headers
                            .entries()
                            .associate { (key, values) -> key to values.joinToString(", ") }
                            .redact(),
                    body = resBody,
                    durationMs = durationMs,
                )
                return@on response
            } catch (t: Throwable) {
                api.error(id = id, message = t.message ?: t::class.simpleName ?: "Unknown error")
                throw t
            }
        }
    }
