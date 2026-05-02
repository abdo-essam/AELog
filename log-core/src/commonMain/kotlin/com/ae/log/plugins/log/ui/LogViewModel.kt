package com.ae.log.plugins.log.ui

import com.ae.log.plugins.log.LogStore
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.plugins.log.model.LogFilter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class LogViewModel(
    private val logStore: LogStore,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    public val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<LogFilter>(com.ae.log.plugins.log.model.LogFilters.ALL)
    public val selectedFilter: StateFlow<LogFilter> = _selectedFilter.asStateFlow()

    public val filteredLogs: StateFlow<List<LogEntry>> =
        combine(
            logStore.dataFlow,
            _searchQuery,
            _selectedFilter,
        ) { logs, query, filter ->
            logs
                .reversed()
                .filter { entry ->
                    filter.matches(entry)
                }.filter { entry ->
                    query.isBlank() ||
                        entry.message.contains(query, ignoreCase = true) ||
                        entry.tag.contains(query, ignoreCase = true)
                }
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    public fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    public fun updateSelectedFilter(filter: LogFilter) {
        _selectedFilter.value = filter
    }

    /** Clear all stored log entries and reset search + filter. */
    public fun clearLogs() {
        logStore.clear()
        _searchQuery.value = ""
        _selectedFilter.value = com.ae.log.plugins.log.model.LogFilters.ALL
    }
}
