package com.ae.log.crashes.ui

import com.ae.log.crashes.model.CrashEvent
import com.ae.log.crashes.model.CrashFilter
import com.ae.log.crashes.storage.CrashStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class CrashViewModel(
    private val storage: CrashStorage,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(CrashFilter.ALL)
    val selectedFilter: StateFlow<CrashFilter> = _selectedFilter.asStateFlow()

    val filteredEvents: StateFlow<List<CrashEvent>> =
        combine(
            storage.events,
            _searchQuery,
            _selectedFilter,
        ) { events: List<CrashEvent>, query: String, filter: CrashFilter ->
            events
                .reversed()
                .filter { filter.matches(it) }
                .filter { event ->
                    query.isBlank() ||
                        event.exceptionType.contains(query, ignoreCase = true) ||
                        event.message.contains(query, ignoreCase = true) ||
                        event.threadName.contains(query, ignoreCase = true)
                }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: CrashFilter) {
        _selectedFilter.value = filter
    }

    fun clearAll() {
        storage.clear()
        _searchQuery.value = ""
        _selectedFilter.value = CrashFilter.ALL
    }
}
