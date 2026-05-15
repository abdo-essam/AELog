package com.ae.log.network

import com.ae.log.AELog

/**
 * Public convenience API accessible via `AELog.network`.
 *
 * Supports two usage patterns:
 *
 * ### One-shot (have all data at once)
 * ```kotlin
 * AELog.network.log(
 *     method = "GET",
 *     url = "https://api.example.com/users",
 *     statusCode = 200,
 *     responseBody = jsonString,
 * )
 * ```
 *
 * ### Two-step (request now, response later)
 * ```kotlin
 * val id = AELog.network.logRequest(method = "GET", url = "https://api.example.com/users")
 * // … network call …
 * AELog.network.logResponse(id = id, statusCode = 200, body = jsonString)
 * ```
 */
public val AELog.network: NetworkProxy
    get() = NetworkProxy

public object NetworkProxy {
    /**
     * Record a complete network call (request + response) in a single call.
     */
    public fun log(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        statusCode: Int? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        responseBody: String? = null,
    ) {
        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return
        val id = recorder.newId()
        recorder.logRequest(id = id, url = url, method = method, headers = headers, body = body)
        if (statusCode != null) {
            recorder.logResponse(id = id, statusCode = statusCode, headers = responseHeaders, body = responseBody)
        }
    }

    /**
     * Record an outgoing request. Returns the entry ID to pass to [logResponse] later.
     *
     * Returns `null` if AELog is not initialized or the network plugin is not installed.
     */
    public fun logRequest(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ): String? {
        val recorder = AELog.getPlugin<NetworkPlugin>()?.recorder ?: return null
        val id = recorder.newId()
        recorder.logRequest(id = id, url = url, method = method, headers = headers, body = body)
        return id
    }

    /**
     * Complete a previously logged request with response data.
     */
    public fun logResponse(
        id: String,
        statusCode: Int,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        durationMs: Long? = null,
    ) {
        AELog.getPlugin<NetworkPlugin>()?.recorder?.logResponse(id, statusCode, body, headers, durationMs)
    }

    /**
     * Record a connection/timeout error for a previously logged request.
     */
    public fun logError(
        id: String,
        message: String,
    ) {
        AELog.getPlugin<NetworkPlugin>()?.recorder?.logError(id, message)
    }

    /**
     * Clear all recorded network traffic.
     */
    public fun clear() {
        AELog.getPlugin<NetworkPlugin>()?.recorder?.clear()
    }
}
