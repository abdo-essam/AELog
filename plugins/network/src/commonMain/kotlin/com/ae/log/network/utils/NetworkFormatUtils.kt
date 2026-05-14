package com.ae.log.network.utils

import com.ae.log.network.model.NetworkEntry
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

private val PRETTY_JSON =
    Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

internal fun String.prettyPrintJson(): String =
    runCatching {
        val jsonElement = PRETTY_JSON.parseToJsonElement(this)
        PRETTY_JSON.encodeToString(JsonElement.serializer(), jsonElement)
    }.getOrDefault(this)

internal fun String.extractQueryParams(): Map<String, String> {
    val queryPart = this.substringAfter('?', "").substringBefore('#')
    if (queryPart.isEmpty()) return emptyMap()

    val params = mutableMapOf<String, String>()
    queryPart.split('&').forEach { param ->
        val parts = param.split('=', limit = 2)
        if (parts.isNotEmpty() && parts[0].isNotEmpty()) {
            params[parts[0]] = parts.getOrNull(1)?.let { decodeUrl(it) } ?: ""
        }
    }
    return params
}

private fun decodeUrl(value: String): String {
    val result = StringBuilder()
    var i = 0
    while (i < value.length) {
        val c = value[i]
        if (c == '+') {
            result.append(' ')
            i++
        } else if (c == '%' && i + 2 < value.length) {
            val hex = value.substring(i + 1, i + 3)
            val code = hex.toIntOrNull(16)
            if (code != null) {
                result.append(code.toChar())
                i += 3
            } else {
                result.append(c)
                i++
            }
        } else {
            result.append(c)
            i++
        }
    }
    return result.toString()
}

/**
 * Produces clipboard text that matches exactly what is displayed on screen:
 *
 * ```
 * POST https://example.com/api/auth
 * Status: 200
 * Duration: 1219ms
 *
 * --- Request Body ---
 * {
 *   "key": "value"
 * }
 *
 * --- Response Body ---
 * {"username":"..."}
 *
 * --- Error ---
 * java.net.UnknownHostException: Unable to resolve host ...
 * ```
 */
internal fun NetworkEntry.toClipboardText(): String =
    buildString {
        // ── Request line ──────────────────────────────────────────────────
        appendLine("${rawMethod.ifBlank { method.label }} $url")

        // ── Status / timing ───────────────────────────────────────────────
        statusCode?.let { appendLine("Status: $it") }
        durationMs?.let { appendLine("Duration: ${it}ms") }

        // ── Request Body ──────────────────────────────────────────────────
        requestBody?.let {
            appendLine()
            appendLine("--- Request Body ---")
            appendLine(it.prettyPrintJson())
        }

        // ── Response Body ─────────────────────────────────────────────────
        responseBody?.let {
            appendLine()
            appendLine("--- Response Body ---")
            appendLine(it.prettyPrintJson())
        }

        // ── Error ─────────────────────────────────────────────────────────
        error?.let {
            appendLine()
            appendLine("--- Error ---")
            appendLine(it)
        }
    }.trimEnd()
