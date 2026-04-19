package com.ae.devlens.plugins.network

import com.ae.devlens.plugins.network.model.NetworkEntry
import com.ae.devlens.plugins.network.model.NetworkMethod
import com.ae.devlens.plugins.network.store.NetworkStore
import kotlin.math.abs
import kotlin.time.Clock

/**
 * Public write-only API for [NetworkPlugin].
 *
 * ```kotlin
 * val api = AEDevLens.default.getPlugin<NetworkPlugin>()?.api
 * val id = newId()
 * api?.request(id, "https://api.example.com/users", NetworkMethod.GET)
 * // ... later ...
 * api?.response(id, statusCode = 200, body = responseBody, durationMs = elapsed)
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
    ): Unit =
        store.record(
            NetworkEntry(
                id = id,
                url = url,
                method = method,
                requestHeaders = headers,
                requestBody = body,
                timestamp = Clock.System.now().toEpochMilliseconds(),
            ),
        )

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
        val existing = store.entries.value.find { it.id == id } ?: return
        store.record(
            existing.copy(
                statusCode = statusCode,
                responseBody = body,
                responseHeaders = headers,
                durationMs = durationMs,
            ),
        )
    }

    /** Record a failed request (connection error, timeout, etc.). */
    public fun error(
        id: String,
        message: String,
    ) {
        val existing = store.entries.value.find { it.id == id } ?: return
        store.record(existing.copy(error = message))
    }

    /** Record a complete request + response entry in one call. */
    public fun record(entry: NetworkEntry): Unit = store.record(entry)

    /** Clear all recorded entries. */
    public fun clear(): Unit = store.clear()

    /** Generate a unique request ID. */
    public fun newId(): String = abs(kotlin.random.Random.nextLong()).toString(36)
}
