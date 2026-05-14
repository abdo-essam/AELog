
package com.ae.log.network

import com.ae.log.AELog

/**
 * Access network recording tools.
 */
public val AELog.network: NetworkProxy
    get() = NetworkProxy

public object NetworkProxy {
    /**
     * Record a one-off network request and response.
     */
    public fun logRequest(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        statusCode: Int? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        responseBody: String? = null,
    ) {
        AELog.getPlugin<NetworkPlugin>()?.recorder?.logRequest(
            method,
            url,
            headers,
            body,
            statusCode,
            responseHeaders,
            responseBody,
        )
    }

    /**
     * Clear all recorded network traffic.
     */
    public fun clear() {
        AELog.getPlugin<NetworkPlugin>()?.recorder?.clear()
    }
}
