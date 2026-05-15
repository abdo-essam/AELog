package com.ae.log.network.config

/**
 * Shared defaults for AELog network interceptors (Ktor + OkHttp).
 *
 * Centralizes header exclusion and body-capture logic so both interceptors
 * stay in sync without copy-pasting.
 */
public object InterceptorDefaults {
    /**
     * Default maximum body size captured by interceptors, in bytes.
     *
     * Matches OkHttp's default. Bodies larger than this are truncated with a `… [truncated]` suffix.
     * Override per-interceptor when instantiating.
     */
    public const val DEFAULT_MAX_BODY_BYTES: Long = 250_000L

    /**
     * Headers excluded from the UI by default.
     *
     * Includes security-sensitive headers (Authorization, Cookie, Set-Cookie)
     * and verbose system headers that add noise without debugging value.
     *
     * Pass an empty set to show all headers, or override with your own list.
     */
    public val DEFAULT_EXCLUDED: Set<String> =
        setOf(
            // Security — never log raw tokens/cookies
            "Authorization",
            "Cookie",
            "Set-Cookie",
            "Proxy-Authorization",
            "X-Api-Key",
            // Auto-injected request headers — noise, set by the HTTP client
            "Accept",
            "Accept-Encoding",
            "Accept-Language",
            "Connection",
            "Content-Type",
            "Host",
            "User-Agent",
            // Noisy system response headers
            "Cache-Control",
            "Date",
            "Expires",
            "Pragma",
            "Server",
            "Strict-Transport-Security",
            "Transfer-Encoding",
            "Vary",
            "X-Content-Type-Options",
            "X-Frame-Options",
            "X-Powered-By",
            "X-XSS-Protection",
        )

    /**
     * Returns a new map with all [excludeHeaders] entries removed (case-insensitive).
     */
    public fun Map<String, String>.exclude(excludeHeaders: Set<String> = DEFAULT_EXCLUDED): Map<String, String> {
        if (excludeHeaders.isEmpty()) return this
        return filter { (key, _) -> excludeHeaders.none { it.equals(key, ignoreCase = true) } }
    }

    /**
     * Returns `true` if the [contentType] should have its body captured as text.
     *
     * @param captureUnknown If `true`, a `null` content-type is treated as capturable
     *   (OkHttp behaviour). Defaults to `false` (Ktor behaviour).
     */
    public fun shouldCaptureBody(
        contentType: String?,
        captureUnknown: Boolean = false,
    ): Boolean {
        if (contentType == null) return captureUnknown
        return contentType.startsWith("text/", ignoreCase = true) ||
            contentType.contains("json", ignoreCase = true) ||
            contentType.contains("xml", ignoreCase = true) ||
            contentType.contains("form-urlencoded", ignoreCase = true)
    }
}
