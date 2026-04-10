package com.ae.devlens.plugins.logs.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.time.Clock

/**
 * Represents a single log entry captured by AEDevLens.
 *
 * Provides computed properties for HTTP parsing, JSON formatting,
 * and log type classification.
 */
data class LogEntry(
    val level: LogLevel,
    val tag: String,
    val message: String,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    // --- Type Classification ---

    val isResponse: Boolean
        get() = message.contains("<--") ||
                message.contains("RESPONSE", ignoreCase = true) ||
                (httpStatusCode != null && !message.contains("-->"))

    val isRequest: Boolean
        get() = !isResponse && (
                message.contains("-->") ||
                        message.contains("REQUEST", ignoreCase = true) ||
                        httpMethod != null
                )

    val isNetworkLog: Boolean
        get() = !isAnalytics && (
                tag.contains("HTTP", ignoreCase = true) ||
                        tag.contains("Network", ignoreCase = true) ||
                        tag.contains("API", ignoreCase = true) ||
                        tag.contains("ktor", ignoreCase = true) ||
                        isRequest ||
                        isResponse ||
                        url != null ||
                        httpMethod != null
                )

    val isError: Boolean
        get() = level == LogLevel.ERROR || level == LogLevel.ASSERT

    val isAnalytics: Boolean
        get() = tag == ANALYTICS_TAG

    // --- HTTP Parsing ---

    val httpMethod: String?
        get() = HTTP_METHODS.firstOrNull { message.contains(it) }

    val httpStatusCode: Int?
        get() = runCatching {
            STATUS_CODE_PATTERN.find(message)?.groupValues?.get(1)?.toIntOrNull()
                ?.takeIf { it in 100..599 }
        }.getOrNull()

    val url: String?
        get() = runCatching {
            URL_PATTERN.find(message)?.value
        }.getOrNull()

    val endpoint: String?
        get() = url?.runCatching {
            val path = substringAfter("://").substringAfter("/").substringBefore("?")
            if (path.isNotEmpty()) "/$path" else null
        }?.getOrNull()

    // --- Display Helpers ---

    val displayTag: String
        get() = when {
            isAnalytics -> "ANALYTICS"
            isError -> "ERROR"
            isResponse -> "RESPONSE"
            isRequest -> "REQUEST"
            isNetworkLog -> "NETWORK"
            else -> "LOG"
        }

    val cleanMessage: String
        get() = message
            .lineSequence()
            .filterNot { line ->
                EXCLUDED_LINES.any { excluded ->
                    line.trim().startsWith(excluded, ignoreCase = true)
                }
            }
            .filterNot { line ->
                line.contains(":") &&
                        HEADER_PATTERNS.any { it.matches(line.trim()) } &&
                        !IMPORTANT_HEADERS.any { line.trim().startsWith(it, ignoreCase = true) }
            }
            .joinToString("\n")
            .trim()
            .ifEmpty { message }

    val jsonBody: String?
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

    private fun extractJsonBlock(text: String, startIndex: Int, open: Char, close: Char): String? {
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
    private fun formatJson(json: String): String {
        return runCatching {
            val jsonElement = PRETTY_JSON.parseToJsonElement(json)
            PRETTY_JSON.encodeToString(JsonElement.serializer(), jsonElement)
        }.getOrDefault(json)
    }

    companion object {
        const val ANALYTICS_TAG = "Analytics"

        private val PRETTY_JSON = Json {
            prettyPrint = true
            prettyPrintIndent = "  "
        }

        private val HTTP_METHODS = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS")

        private val STATUS_CODE_PATTERN by lazy {
            Regex("\\b([1-5][0-9]{2})\\b")
        }

        private val URL_PATTERN by lazy {
            Regex("https?://[^\\s\"'\\]\\)>]+")
        }

        private val HEADER_PATTERNS by lazy {
            listOf(Regex("^[A-Za-z][A-Za-z0-9-]*:\\s*.+$"))
        }

        private val EXCLUDED_LINES = listOf(
            "COMMON HEADERS",
            "CONTENT HEADERS",
            "BODY START",
            "BODY END",
            "REQUEST",
            "RESPONSE",
            "HEADERS",
            "-->",
            "<--"
        )

        private val IMPORTANT_HEADERS = listOf(
            "Content-Type",
            "Authorization"
        )
    }
}
