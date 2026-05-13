package com.ae.log.plugins.network

import com.ae.log.AELog
import com.ae.log.plugins.network.model.NetworkEntry
import com.ae.log.plugins.network.model.NetworkMethod
import com.ae.log.plugins.network.storage.NetworkStorage
import com.ae.log.utils.IdGenerator
import kotlin.time.Clock

/**
 * Low-level write API for [NetworkPlugin].
 */
public class NetworkRecorder internal constructor(
    private val storage: NetworkStorage,
    private val clock: Clock = Clock.System,
    private val idGenerator: () -> String = {
        IdGenerator.next()
    },
) {
    /** Record a full request + response in a single call. */
    public fun logRequest(
        method: String,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
        statusCode: Int? = null,
        responseHeaders: Map<String, String> = emptyMap(),
        responseBody: String? = null,
    ) {
        if (!AELog.isEnabled) return
        val id = newId()
        storage.recordOrReplace(
            NetworkEntry(
                id = id,
                url = url,
                method = NetworkMethod.fromString(method),
                rawMethod = method,
                requestHeaders = headers,
                requestBody = body,
                responseHeaders = responseHeaders,
                responseBody = responseBody,
                statusCode = statusCode,
                timestamp = clock.now().toEpochMilliseconds(),
            ),
        )
    }

    /** Start recording a request that will be completed later. */
    public fun startRequest(
        id: String,
        url: String,
        method: NetworkMethod,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ) {
        if (!AELog.isEnabled) return
        storage.recordOrReplace(
            NetworkEntry(
                id = id,
                url = url,
                method = method,
                rawMethod = method.name,
                requestHeaders = headers,
                requestBody = body,
                timestamp = clock.now().toEpochMilliseconds(),
            ),
        )
    }

    /** Finish a previously started request. */
    public fun logResponse(
        id: String,
        statusCode: Int,
        body: String? = null,
        headers: Map<String, String> = emptyMap(),
        durationMs: Long? = null,
    ) {
        if (!AELog.isEnabled) return
        storage.update(id) { existing ->
            existing.copy(
                statusCode = statusCode,
                responseBody = body,
                responseHeaders = headers,
                durationMs = durationMs,
            )
        }
    }

    /** Patch the response body after an entry has already been recorded. */
    public fun updateResponseBody(
        id: String,
        body: String?,
    ) {
        if (!AELog.isEnabled) return
        storage.update(id) { it.copy(responseBody = body) }
    }

    /** Patch the request body after an entry has already been started (e.g. after serialization). */
    public fun updateRequestBody(
        id: String,
        body: String?,
    ) {
        if (!AELog.isEnabled) return
        storage.update(id) { it.copy(requestBody = body) }
    }

    /** Record a failed request. */
    public fun logError(
        id: String,
        message: String,
    ) {
        if (!AELog.isEnabled) return
        storage.update(id) { it.copy(error = message) }
    }

    internal fun recordOrReplace(entry: NetworkEntry) {
        if (!AELog.isEnabled) return
        storage.recordOrReplace(entry)
    }

    public fun clear(): Unit = storage.clear()

    public fun newId(): String = idGenerator()
}
