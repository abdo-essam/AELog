package com.ae.log.plugins.network.model

/** HTTP method of the request. */
public enum class NetworkMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS,

    /** Catch-all for verbs not in this enum (e.g. CONNECT, TRACE, custom verbs). */
    UNKNOWN,
    ;

    public val label: String get() = name

    public companion object {
        /**
         * Case-insensitive conversion from a raw HTTP verb string.
         *
         * Returns [UNKNOWN] for verbs not in this enum so that the original
         * verb is preserved in [NetworkEntry.rawMethod] and displayed correctly.
         *
         * ```kotlin
         * NetworkMethod.fromString("post")    // → POST
         * NetworkMethod.fromString("CONNECT") // → UNKNOWN (rawMethod = "CONNECT")
         * ```
         */
        public fun fromString(value: String): NetworkMethod =
            entries.firstOrNull {
                it != UNKNOWN && it.name.equals(value, ignoreCase = true)
            } ?: UNKNOWN
    }
}
