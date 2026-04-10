package com.ae.devlens.plugins.logs.store

import com.ae.devlens.plugins.logs.model.LogEntry
import com.ae.devlens.plugins.logs.model.LogFilter
import com.ae.devlens.plugins.logs.model.LogLevel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Thread-safe log storage.
 *
 * Each log emits immediately to the StateFlow — no batching, no delay.
 * Safe to call from any thread or coroutine context.
 */
class LogStore(private val maxEntries: Int = 500) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val mutex = Mutex()
    private val _logsList = mutableListOf<LogEntry>()

    private val _logsFlow = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsFlow: StateFlow<List<LogEntry>> = _logsFlow.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(LogFilter.ALL)
    val selectedFilter: StateFlow<LogFilter> = _selectedFilter.asStateFlow()

    /**
     * Log a new entry immediately. Safe to call from any thread.
     * Uses a coroutine to avoid blocking the caller.
     */
    fun log(level: LogLevel, tag: String, message: String) {
        scope.launch {
            mutex.withLock {
                _logsList.add(LogEntry(level, tag, message))
                if (_logsList.size > maxEntries) {
                    _logsList.removeAt(0)
                }
                _logsFlow.value = _logsList.toList()
            }
        }
    }

    fun updateSearchQuery(query: String) { _searchQuery.value = query }
    fun updateSelectedFilter(filter: LogFilter) { _selectedFilter.value = filter }

    fun clear() {
        scope.launch {
            mutex.withLock {
                _logsList.clear()
                _logsFlow.value = emptyList()
            }
        }
    }
}
