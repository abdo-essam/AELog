package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.network.model.NetworkEntry
import com.ae.log.network.model.NetworkMethod
import com.ae.log.network.storage.NetworkStorage
import com.ae.log.utils.IdGenerator
import kotlin.time.Clock

/**
 * Low-level write API for [NetworkPlugin].
 *
 * All interceptors and the public proxy use the same two-step flow:
 *
 * ```
 * val id = recorder.newId()
 * recorder.logRequest(id, url, method, ...)   // pending entry created
 * // … network call …
 * recorder.logResponse(id, statusCode, ...)   // entry completed
 * ```
 */
public class NetworkRecorder internal constructor(
    private val storage: NetworkStorage,
    private val clock: Clock = Clock.System,
    private val idGenerator: () -> String = { IdGenerator.next() },
) {
    // ── Record a request (creates a pending entry) ────────────────────────

    /** Record an outgoing request. Call [logResponse] later to complete it. */
    public fun logRequest(
        id: String,
        url: String,
        method: String,
        headers: Map<String, String> = emptyMap(),
        body: String? = null,
    ) {
        if (!AELog.isEnabled) return
        storage.recordOrReplace(
            NetworkEntry(
                id = id,
                url = url,
                method = NetworkMethod.fromString(method),
                rawMethod = method,
                requestHeaders = headers,
                requestBody = body,
                timestamp = clock.now().toEpochMilliseconds(),
            ),
        )
    }

    // ── Complete a request with response data ─────────────────────────────

    /** Finish a previously logged request with response data. */
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

    // ── Patch helpers (used by interceptors after initial recording) ──────

    /** Patch the response body after an entry has already been recorded. */
    public fun updateResponseBody(id: String, body: String?) {
        if (!AELog.isEnabled) return
        storage.update(id) { it.copy(responseBody = body) }
    }

    /** Patch the request body after serialization completes. */
    public fun updateRequestBody(id: String, body: String?) {
        if (!AELog.isEnabled) return
        storage.update(id) { it.copy(requestBody = body) }
    }

    /** Record a connection/timeout error for a previously logged request. */
    public fun logError(id: String, message: String) {
        if (!AELog.isEnabled) return
        storage.update(id) { it.copy(error = message) }
    }

    public fun clear(): Unit = storage.clear()

    public fun newId(): String = idGenerator()
}
