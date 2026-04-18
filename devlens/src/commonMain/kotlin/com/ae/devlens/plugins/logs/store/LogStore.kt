package com.ae.devlens.plugins.logs.store

import com.ae.devlens.plugins.logs.model.LogEntry
import com.ae.devlens.plugins.logs.model.LogFilter
import com.ae.devlens.plugins.logs.model.LogSeverity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe log storage.
 *
 * Each log emits immediately to the StateFlow — no batching, no delay.
 * Safe to call from any thread or coroutine context.
 */
public class LogStore(
    private val maxEntries: Int = 500,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    public val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    public val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(LogFilter.ALL)
    public val selectedFilter: StateFlow<LogFilter> = _selectedFilter.asStateFlow()

    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
    ) {
        val entry = LogEntry(severity = severity, tag = tag, message = message)
        _logsFlow.update { current ->
            val updated = current.toMutableList()
            updated.add(entry)
            if (updated.size > maxEntries) {
                updated.removeAt(0)
            }
            updated
        }
    }

    public fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    public fun updateSelectedFilter(filter: LogFilter) {
        _selectedFilter.value = filter
    }

    public fun clear() {
        _logsFlow.value = emptyList()
    }

    public fun destroy() {
        scope.cancel()
    }
}
