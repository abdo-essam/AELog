package com.ae.logs.plugins.network.ui

import com.ae.logs.plugins.network.model.NetworkEntry
import com.ae.logs.plugins.network.model.NetworkFilter
import com.ae.logs.plugins.network.store.NetworkStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** Controls search/filter UI state for the network monitor panel. */
internal class NetworkViewModel(
    private val store: NetworkStore,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow<NetworkFilter>(com.ae.logs.plugins.network.model.NetworkFilters.ALL)
    val filter: StateFlow<NetworkFilter> = _filter.asStateFlow()

    /** Filtered + reversed (newest first) entry list. */
    val filteredEntries: StateFlow<List<NetworkEntry>> =
        combine(
            store.entries,
            _searchQuery,
            _filter,
        ) { all, query, filter ->
            all
                .reversed()
                .filter { entry ->
                    val matchesQuery =
                        query.isBlank() ||
                            entry.url.contains(query, ignoreCase = true) ||
                            entry.method.label.contains(query, ignoreCase = true) ||
                            entry.statusCode?.toString()?.contains(query) == true ||
                            entry.requestBody?.contains(query, ignoreCase = true) == true ||
                            entry.responseBody?.contains(query, ignoreCase = true) == true
                    val matchesFilter = filter.matches(entry)
                    matchesQuery && matchesFilter
                }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(f: NetworkFilter) {
        _filter.value = f
    }

    fun clear() {
        store.clear()
        _searchQuery.value = ""
        _filter.value = com.ae.logs.plugins.network.model.NetworkFilters.ALL
    }

    val hasPending: StateFlow<Boolean> =
        store.entries
            .map { entries -> entries.any { it.isPending } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
}
