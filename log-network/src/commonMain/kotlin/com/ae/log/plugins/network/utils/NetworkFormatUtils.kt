@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.ae.log.plugins.network.utils

import com.ae.log.plugins.network.model.NetworkEntry
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
            params[parts[0]] = parts.getOrNull(1)?.let {
                // simple URL decoding for query params display
                it.replace("+", " ").replace("%20", " ") // A very basic decode, you can improve if needed
            } ?: ""
        }
    }
    return params
}

internal fun NetworkEntry.toClipboardText(): String =
    buildString {
        appendLine("${method.label} $url")
        statusCode?.let { appendLine("Status: $it") }
        durationMs?.let { appendLine("Duration: ${it}ms") }
        requestBody?.let { appendLine("\n--- Request Body ---\n$it") }
        responseBody?.let { appendLine("\n--- Response Body ---\n$it") }
        error?.let { appendLine("\n--- Error ---\n$it") }
    }
