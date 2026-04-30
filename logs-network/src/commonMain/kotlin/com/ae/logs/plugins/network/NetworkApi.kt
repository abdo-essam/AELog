package com.ae.logs.plugins.network

import com.ae.logs.plugins.network.model.NetworkEntry
import com.ae.logs.plugins.network.model.NetworkMethod
import com.ae.logs.plugins.network.store.NetworkStore
import kotlin.time.Clock

/**
 * Low-level write API for [NetworkPlugin].
 *
 * **Prefer the first-class interceptors** — they handle ID management, timing,
 * and error recording automatically:
 *
 * - Ktor: `install(AELogsKtorPlugin)` — see [com.ae.logs.plugins.network.interceptor.AELogsKtorPlugin]
 * - OkHttp: `.addInterceptor(AELogsOkHttpInterceptor())` — see [com.ae.logs.plugins.network.interceptor.AELogsOkHttpInterceptor]
 *
 * Use this API directly only for custom or unsupported HTTP clients:
 *
 * ```kotlin
 * val api = AELogs.plugin<NetworkPlugin>()?.api ?: return
 * val id  = api.newId()
 *
 * api.request(id, "https://api.example.com/users", NetworkMethod.GET)
 * // … perform the request …
 * api.response(id, statusCode = 200, body = body, durationMs = elapsed)
 * // or on failure:
 * api.error(id, "Connection timed out")
 * ```
 */
public class NetworkApi internal constructor(
    private val store: NetworkStore,
) {
    /**
     * Record the start of an outgoing request.
     * @param id Stable unique ID — use the same ID when calling [response] or [error].
     */
    public fun request(
        id: String,
        url: String,
        method: NetworkMethod,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ) {
        if (!com.ae.logs.AELogs.isEnabled) return
        store.recordOrReplace(
            NetworkEntry(
                id = id,
                url = url,
                method = method,
                requestHeaders = headers,
                requestBody = body,
                timestamp = Clock.System.now().toEpochMilliseconds(),
            ),
        )
    }

    /**
     * Update an existing request with its response data.
     * @param id Must match the ID passed to [request].
     */
    public fun response(
        id: String,
        statusCode: Int,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
        durationMs: Long? = null,
    ) {
        if (!com.ae.logs.AELogs.isEnabled) return
        store.update(id) { existing ->
            existing.copy(
                statusCode = statusCode,
                responseBody = body,
                responseHeaders = headers,
                durationMs = durationMs,
            )
        }
    }

    /** Record a failed request (connection error, timeout, etc.). */
    public fun error(
        id: String,
        message: String,
    ) {
        if (!com.ae.logs.AELogs.isEnabled) return
        store.update(id) { existing ->
            existing.copy(error = message)
        }
    }

    /** Record a complete request + response entry in one call. */
    public fun recordOrReplace(entry: NetworkEntry) {
        if (!com.ae.logs.AELogs.isEnabled) return
        store.recordOrReplace(entry)
    }

    /**
     * Higher-level helper to track a network call automatically.
     * Generates an ID, records the request, times the execution, and logs the result or error.
     *
     * ```kotlin
     * val data = api.recordCall(url, NetworkMethod.GET) {
     *     val response = httpClient.execute(...)
     *     NetworkResult(response.data, response.code, response.bodyString)
     * }
     * ```
     */
    public inline fun <T> recordCall(
        url: String,
        method: NetworkMethod,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        block: () -> NetworkResult<T>,
    ): T {
        val id = newId()
        request(id, url, method, headers, body)
        val start = Clock.System.now().toEpochMilliseconds()

        return try {
            val result = block()
            val durationMs = Clock.System.now().toEpochMilliseconds() - start
            response(
                id = id,
                statusCode = result.statusCode,
                body = result.body,
                headers = result.headers,
                durationMs = durationMs,
            )
            result.value
        } catch (e: Throwable) {
            error(id, e.message ?: e.toString())
            throw e
        }
    }

    /** Clear all recorded entries. */
    public fun clear(): Unit = store.clear()

    /** Generate a unique request ID. */
    public fun newId(): String =
        com.ae.logs.core.utils.IdGenerator
            .generateId()
}

/**
 * Return type for [NetworkApi.recordCall] which encapsulates the raw network details
 * alongside the parsed or domain value.
 */
public class NetworkResult<out T>(
    public val value: T,
    public val statusCode: Int,
    public val body: String? = null,
    public val headers: Map<String, String> = emptyMap(),
)
