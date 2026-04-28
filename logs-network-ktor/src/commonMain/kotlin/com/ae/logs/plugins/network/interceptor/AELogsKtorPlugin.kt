package com.ae.logs.plugins.network.interceptor

import com.ae.logs.AELogs
import com.ae.logs.plugins.network.NetworkPlugin
import com.ae.logs.plugins.network.model.NetworkMethod
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
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

public val AELogsKtorPlugin: ClientPlugin<Unit> =
    createClientPlugin("AELogsKtor") {
        on(io.ktor.client.plugins.api.Send) { request ->
            val api = AELogs.plugin<NetworkPlugin>()?.api ?: return@on proceed(request)

            val id = api.newId()
            val startMs =
                Clock.System
                    .now()
                    .toEpochMilliseconds()

            api.request(
                id = id,
                url = request.url.buildString(),
                method = NetworkMethod.fromString(request.method.value),
                headers =
                    request.headers
                        .entries()
                        .associate { (key, values) -> key to values.joinToString(", ") },
            )

            try {
                val response = proceed(request)
                val durationMs =
                    Clock.System
                        .now()
                        .toEpochMilliseconds() - startMs

                api.response(
                    id = id,
                    statusCode = response.response.status.value,
                    headers =
                        response.response.headers
                            .entries()
                            .associate { (key, values) -> key to values.joinToString(", ") },
                    durationMs = durationMs,
                )
                return@on response
            } catch (e: Exception) {
                api.error(id = id, message = e.message ?: e.toString())
                throw e
            }
        }
    }
