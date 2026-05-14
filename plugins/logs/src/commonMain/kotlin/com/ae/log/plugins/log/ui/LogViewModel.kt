package com.ae.log.plugins.log.ui

import com.ae.log.plugins.log.LogStorage
import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.plugins.log.model.LogSeverityFilter
import com.ae.log.plugins.log.model.LogSeverityFilters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

internal class LogViewModel(
    private val logStorage: LogStorage,
    scope: CoroutineScope,
) {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<LogSeverityFilter>(LogSeverityFilters.ALL)
    val selectedFilter: StateFlow<LogSeverityFilter> = _selectedFilter.asStateFlow()

    val filteredLogs: StateFlow<List<LogEntry>> =
        combine(
            logStorage.dataFlow,
            _searchQuery,
            _selectedFilter,
        ) { logs: List<LogEntry>, query: String, filter: LogSeverityFilter ->
            logs
                .reversed()
                .filter { entry -> filter.matches(entry) }
                .filter { entry ->
                    query.isBlank() ||
                        entry.message.contains(query, ignoreCase = true) ||
                        entry.tag.contains(query, ignoreCase = true)
                }
        }.stateIn(scope = scope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedFilter(filter: LogSeverityFilter) {
        _selectedFilter.value = filter
    }

    fun clearLogs() {
        logStorage.clear()
        _searchQuery.value = ""
        _selectedFilter.value = LogSeverityFilters.ALL
    }
}
