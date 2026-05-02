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
    ;

    public val label: String get() = name

    public companion object {
        /**
         * Case-insensitive conversion from a raw HTTP verb string.
         *
         * Returns [GET] as a safe fallback for unknown verbs so interceptors
         * never have to deal with null.
         *
         * ```kotlin
         * NetworkMethod.fromString("post")  // → POST
         * NetworkMethod.fromString("PATCH") // → PATCH
         * ```
         */
        public fun fromString(value: String): NetworkMethod =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GET
    }
}
