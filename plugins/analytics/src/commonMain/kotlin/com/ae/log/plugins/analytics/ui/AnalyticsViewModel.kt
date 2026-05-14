package com.ae.log.plugins.analytics.ui

import com.ae.log.plugins.analytics.model.AnalyticsEvent
import com.ae.log.plugins.analytics.model.AnalyticsFilter
import com.ae.log.plugins.analytics.model.AnalyticsFilters
import com.ae.log.plugins.analytics.storage.AnalyticsStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Controls search/filter UI state for the analytics panel. */
internal class AnalyticsViewModel(
    private val storage: AnalyticsStorage,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow<AnalyticsFilter>(AnalyticsFilters.ALL)
    val filter: StateFlow<AnalyticsFilter> = _filter.asStateFlow()

    /** Filtered + reversed (newest first) event list. */
    val filteredEvents: StateFlow<List<AnalyticsEvent>> =
        combine(
            storage.events,
            _searchQuery,
            _filter,
        ) { all, query, filter ->
            all
                .reversed()
                .filter { event ->
                    val matchesQuery =
                        query.isBlank() ||
                            event.name.contains(query, ignoreCase = true) ||
                            event.source?.sourceName?.contains(query, ignoreCase = true) == true ||
                            event.properties.any { (k, v) ->
                                k.contains(query, ignoreCase = true) || v.toString().contains(query, ignoreCase = true)
                            }
                    val matchesFilter = filter.matches(event)
                    matchesQuery && matchesFilter
                }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(f: AnalyticsFilter) {
        _filter.value = f
    }

    fun clear() {
        storage.clear()
        _searchQuery.value = ""
        _filter.value = AnalyticsFilters.ALL
    }
}
