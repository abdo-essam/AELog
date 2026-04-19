package com.ae.devlens.plugins.network.model

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
}
