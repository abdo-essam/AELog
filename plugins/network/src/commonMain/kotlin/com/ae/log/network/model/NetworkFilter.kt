package com.ae.log.network.model

import com.ae.log.plugin.PluginFilter

/**
 * Filter options for the network panel.
 *
 * Extends [PluginFilter] so the label/matches contract is shared across
 * all plugin filter hierarchies.
 */
public open class NetworkFilter(
    label: String,
    predicate: (NetworkEntry) -> Boolean,
) : PluginFilter<NetworkEntry>(label, predicate)

public object NetworkFilters {
    public val ALL: NetworkFilter = NetworkFilter("All") { true }
    public val PENDING: NetworkFilter = NetworkFilter("Pending") { it.isPending }
    public val SUCCESS: NetworkFilter = NetworkFilter("Success") { it.isSuccess }
    public val ERRORS: NetworkFilter = NetworkFilter("Errors") { it.isError }

    public val defaultFilters: List<NetworkFilter> = listOf(ALL, PENDING, SUCCESS, ERRORS)
}
