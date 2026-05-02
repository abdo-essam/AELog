package com.ae.log.plugins.log.model

import kotlinx.atomicfu.update
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

public object LogTagRegistry {
    private val tags = kotlinx.atomicfu.atomic(emptyMap<String, String>())

    public fun register(
        tag: String,
        label: String,
    ) {
        tags.update { it + (tag to label) }
    }

    public fun isRegistered(tag: String): Boolean = tags.value.containsKey(tag)

    public fun getLabel(tag: String): String? = tags.value[tag]
}

private val PRETTY_JSON =
    Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

private val HTTP_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")

private val STATUS_CODE_PATTERN by lazy { Regex("(?:HTTP/\\d\\.\\d\\s+|status[:\\s]+)(\\d{3})") }
private val URL_PATTERN by lazy { Regex("https?://[^\\s\"'\\]\\)>]+") }
private val HEADER_PATTERNS by lazy { listOf(Regex("^[A-Za-z][A-Za-z0-9-]*:\\s*.+$")) }

private val EXCLUDED_LINES =
    listOf(
        "COMMON HEADERS",
        "CONTENT HEADERS",
        "BODY START",
        "BODY END",
        "REQUEST",
        "RESPONSE",
        "HEADERS",
        "-->",
        "<--",
    )

private val IMPORTANT_HEADERS = listOf("Content-Type", "Authorization")

// --- Type Classification ---
public val LogEntry.isResponse: Boolean
    get() =
        message.contains("<--") ||
            message.contains("RESPONSE", ignoreCase = true) ||
            (httpStatusCode != null && !message.contains("-->"))

public val LogEntry.isRequest: Boolean
    get() =
        !isResponse &&
            (message.contains("-->") || message.contains("REQUEST", ignoreCase = true) || httpMethod != null)

public val LogEntry.isNetworkLog: Boolean
    get() =
        !LogTagRegistry.isRegistered(tag) &&
            (
                tag.contains("HTTP", ignoreCase = true) ||
                    tag.contains("Network", ignoreCase = true) ||
                    tag.contains("API", ignoreCase = true) ||
                    tag.contains("ktor", ignoreCase = true) ||
                    isRequest ||
                    isResponse ||
                    url != null ||
                    httpMethod != null
            )

public val LogEntry.isError: Boolean
    get() = severity == LogSeverity.ERROR || severity == LogSeverity.ASSERT

public val LogEntry.isAnalytics: Boolean
    get() = LogTagRegistry.isRegistered(tag)

// --- HTTP Parsing ---
public val LogEntry.httpMethod: String?
    get() = HTTP_METHODS.firstOrNull { message.contains(it) }

public val LogEntry.httpStatusCode: Int?
    get() =
        runCatching {
            STATUS_CODE_PATTERN
                .find(message)
                ?.groupValues
                ?.get(1)
                ?.toIntOrNull()
                ?.takeIf { it in 100..599 }
        }.getOrNull()

public val LogEntry.url: String?
    get() = runCatching { URL_PATTERN.find(message)?.value }.getOrNull()

public val LogEntry.endpoint: String?
    get() =
        url
            ?.runCatching {
                val path = substringAfter("://").substringAfter("/").substringBefore("?")
                if (path.isNotEmpty()) "/$path" else null
            }?.getOrNull()

// --- Display Helpers ---
public val LogEntry.displayTag: String
    get() =
        when {
            isAnalytics -> LogTagRegistry.getLabel(tag)?.uppercase() ?: tag.uppercase()
            isError -> "ERROR"
            isResponse -> "RESPONSE"
            isRequest -> "REQUEST"
            isNetworkLog -> "NETWORK"
            else -> "LOG"
        }

public val LogEntry.cleanMessage: String
    get() =
        message
            .lineSequence()
            .filterNot { line ->
                EXCLUDED_LINES.any { excluded ->
                    line.trim().startsWith(excluded, ignoreCase = true)
                }
            }.filterNot { line ->
                line.contains(":") &&
                    HEADER_PATTERNS.any { it.matches(line.trim()) } &&
                    !IMPORTANT_HEADERS.any { line.trim().startsWith(it, ignoreCase = true) }
            }.joinToString("\n")
            .trim()
            .ifEmpty { message }

public val LogEntry.jsonBody: String?
    get() = extractJson(message)?.let { formatJson(it) }

// --- JSON Extraction ---
private fun extractJson(text: String): String? {
    val arrayStart = text.indexOf('[')
    val objectStart = text.indexOf('{')
    return when {
        arrayStart == -1 && objectStart == -1 -> null
        arrayStart == -1 -> extractJsonBlock(text, objectStart, '{', '}')
        objectStart == -1 -> extractJsonBlock(text, arrayStart, '[', ']')
        arrayStart < objectStart -> extractJsonBlock(text, arrayStart, '[', ']')
        else -> extractJsonBlock(text, objectStart, '{', '}')
    }
}

private fun extractJsonBlock(
    text: String,
    startIndex: Int,
    open: Char,
    close: Char,
): String? {
    var count = 0
    for (i in startIndex until text.length) {
        when (text[i]) {
            open -> count++
            close -> {
                count--
                if (count == 0) return text.substring(startIndex, i + 1)
            }
        }
    }
    return null
}

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
private fun formatJson(json: String): String =
    runCatching {
        val jsonElement = PRETTY_JSON.parseToJsonElement(json)
        PRETTY_JSON.encodeToString(JsonElement.serializer(), jsonElement)
    }.getOrDefault(json)
