package com.ae.devlens.plugins.network.model

/** Filter options for the network panel. */
public enum class NetworkFilter(
    public val label: String,
) {
    ALL("All"),
    PENDING("Pending"),
    SUCCESS("2xx"),
    CLIENT_ERROR("4xx"),
    SERVER_ERROR("5xx"),
    FAILED("Failed"),
}
