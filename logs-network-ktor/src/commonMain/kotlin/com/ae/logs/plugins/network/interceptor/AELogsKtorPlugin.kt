package com.ae.logs.plugins.network.interceptor

import com.ae.logs.AELogs
import com.ae.logs.plugins.network.NetworkPlugin
import com.ae.logs.plugins.network.model.NetworkMethod
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.util.AttributeKey
import kotlin.time.Clock

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
 * implementation("io.github.abdo-essam:logs-network-ktor:<version>")
 * ```
 *
 * Install the plugin — one line:
 * ```kotlin
 * val client = HttpClient(CIO) {
 *     install(AELogsKtorPlugin)
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
 * If [NetworkPlugin] is not installed or [AELogs.init] has not been called,
 * every hook returns immediately — the real HTTP client is never affected.
 */
public val AELogsKtorPlugin: ClientPlugin<Unit> = createClientPlugin("AELogsKtor") {

    /** Carries the generated request ID from [onRequest] through to [onResponse]. */
    val requestIdAttr = AttributeKey<String>("AELogsRequestId")

    /** Wall-clock start time for duration calculation. */
    val startTimeAttr = AttributeKey<Long>("AELogsRequestStart")

    onRequest { request, _ ->
        val api = AELogs.plugin<NetworkPlugin>()?.api ?: return@onRequest

        val id = api.newId()
        val startMs = Clock.System.now().toEpochMilliseconds()

        request.attributes.put(requestIdAttr, id)
        request.attributes.put(startTimeAttr, startMs)

        api.request(
            id = id,
            url = request.url.buildString(),
            method = NetworkMethod.fromString(request.method.value),
            headers = request.headers.entries()
                .associate { (key, values) -> key to values.joinToString(", ") },
        )
    }

    onResponse { response ->
        val api = AELogs.plugin<NetworkPlugin>()?.api ?: return@onResponse

        val attrs = response.call.request.attributes
        val id = attrs.getOrNull(requestIdAttr) ?: return@onResponse
        val startMs = attrs.getOrNull(startTimeAttr) ?: 0L
        val durationMs = Clock.System.now().toEpochMilliseconds() - startMs

        api.response(
            id = id,
            statusCode = response.status.value,
            headers = response.headers.entries()
                .associate { (key, values) -> key to values.joinToString(", ") },
            durationMs = durationMs,
        )
    }
}
