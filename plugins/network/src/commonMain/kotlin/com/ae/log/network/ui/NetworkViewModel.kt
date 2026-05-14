package com.ae.log.network.ui

import com.ae.log.network.model.NetworkEntry
import com.ae.log.network.model.NetworkFilter
import com.ae.log.network.model.NetworkFilters
import com.ae.log.network.storage.NetworkStorage
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
    private val storage: NetworkStorage,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow<NetworkFilter>(NetworkFilters.ALL)
    val filter: StateFlow<NetworkFilter> = _filter.asStateFlow()

    /** Filtered + reversed (newest first) entry list. */
    val filteredEntries: StateFlow<List<NetworkEntry>> =
        combine(
            storage.entries,
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
        storage.clear()
        _searchQuery.value = ""
        _filter.value = NetworkFilters.ALL
    }

    val hasPending: StateFlow<Boolean> =
        storage.entries
            .map { entries -> entries.any { it.isPending } }
            .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)
}
