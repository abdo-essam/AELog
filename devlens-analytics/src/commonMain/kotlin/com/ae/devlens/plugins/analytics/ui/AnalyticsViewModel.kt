package com.ae.devlens.plugins.analytics.ui

import com.ae.devlens.plugins.analytics.model.AnalyticsEvent
import com.ae.devlens.plugins.analytics.model.AnalyticsFilter
import com.ae.devlens.plugins.analytics.store.AnalyticsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Controls search/filter UI state for the analytics panel. */
internal class AnalyticsViewModel(
    private val store: AnalyticsStore,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filter = MutableStateFlow(AnalyticsFilter.ALL)
    val filter: StateFlow<AnalyticsFilter> = _filter.asStateFlow()

    /** Filtered + reversed (newest first) event list. */
    val filteredEvents: StateFlow<List<AnalyticsEvent>> =
        combine(
            store.events,
            _searchQuery,
            _filter,
        ) { all, query, filter ->
            all
                .reversed()
                .filter { event ->
                    val matchesQuery =
                        query.isBlank() ||
                            event.name.contains(query, ignoreCase = true) ||
                            event.source?.contains(query, ignoreCase = true) == true ||
                            event.properties.any { (k, v) ->
                                k.contains(query, ignoreCase = true) || v.contains(query, ignoreCase = true)
                            }
                    val matchesFilter =
                        when (filter) {
                            AnalyticsFilter.ALL -> true
                            AnalyticsFilter.SCREENS -> event.name == "screen_view"
                            AnalyticsFilter.EVENTS -> event.name != "screen_view"
                        }
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
        store.clear()
        _searchQuery.value = ""
        _filter.value = AnalyticsFilter.ALL
    }
}
