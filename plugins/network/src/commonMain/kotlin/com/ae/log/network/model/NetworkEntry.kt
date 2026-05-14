package com.ae.log.network.model

import androidx.compose.runtime.Immutable

/** A single intercepted HTTP request/response pair. */
@Immutable
public data class NetworkEntry(
    /** Unique ID — use the same ID for request → response updates. */
    val id: String,
    val url: String,
    val method: NetworkMethod,
    val rawMethod: String = method.name,
    /** `null` while the request is in-flight. */
    val statusCode: Int? = null,
    val requestBody: String? = null,
    val responseBody: String? = null,
    val requestHeaders: Map<String, String> = emptyMap(),
    val responseHeaders: Map<String, String> = emptyMap(),
    /** `null` while the request is in-flight. */
    val durationMs: Long? = null,
    /** Set by [com.ae.log.network.NetworkRecorder] at record time (epoch millis). */
    val timestamp: Long,
    /** Populated when the request fails with a connection/timeout error. */
    val error: String? = null,
) {
    /** `true` if the response status is 2xx. */
    public val isSuccess: Boolean get() = statusCode != null && statusCode in 200..299

    /** `true` if the response is a client/server error or a connection failure. */
    public val isError: Boolean get() = error != null || (statusCode != null && statusCode >= 400)

    /** `true` if the request has not yet received a response. */
    public val isPending: Boolean get() = statusCode == null && error == null

    /** Short human-readable status label. */
    public val statusLabel: String
        get() =
            when {
                isPending -> "…"
                statusCode != null -> statusCode.toString() // always show code when available
                error != null -> "Error" // connection failure, no code
                else -> "…"
            }
}
