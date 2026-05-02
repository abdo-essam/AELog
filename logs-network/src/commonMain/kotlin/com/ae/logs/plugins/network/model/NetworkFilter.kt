package com.ae.logs.plugins.network.model

/** Filter options for the network panel. */
public open class NetworkFilter(
    public val label: String,
    private val predicate: (NetworkEntry) -> Boolean,
) {
    public open fun matches(entry: NetworkEntry): Boolean = predicate(entry)
}

public object NetworkFilters {
    public val ALL: NetworkFilter = NetworkFilter("All") { true }
    public val PENDING: NetworkFilter = NetworkFilter("Pending") { it.isPending }
    public val SUCCESS: NetworkFilter = NetworkFilter("2xx") { it.isSuccess }
    public val ERRORS: NetworkFilter = NetworkFilter("Errors") { it.isError }

    public val defaultFilters: List<NetworkFilter> = listOf(ALL, PENDING, SUCCESS, ERRORS)
}
