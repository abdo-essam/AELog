package com.ae.logs.plugins.logs.store

import com.ae.logs.core.store.PluginStore
import com.ae.logs.plugins.logs.model.LogEntry
import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe log storage backed by [PluginStore].
 *
 * Stores up to [maxEntries] log entries in a fixed-capacity ring buffer.
 * Each call to [log] immediately emits the updated list to [logsFlow].
 *
 * UI state (search query, filters) lives in `LogsViewModel`, not here.
 */
public class LogStore(
    private val maxEntries: Int = 500,
) {
    private val store = PluginStore<LogEntry>(capacity = maxEntries)

    /** Reactive stream of all stored log entries (oldest first). */
    public val logsFlow: StateFlow<List<LogEntry>> = store.dataFlow

    /** Add a new log entry. */
    public fun log(entry: LogEntry) {
        store.add(entry)
    }

    /** Remove all stored entries. */
    public fun clear() {
        store.clear()
    }

    /** Current number of stored entries. */
    public val count: Int get() = store.count
}
