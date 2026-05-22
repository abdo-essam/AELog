package com.ae.log.logs.storage

import com.ae.log.logs.model.LogEntry
import com.ae.log.storage.InMemoryPluginStorage
import kotlinx.coroutines.flow.StateFlow

/**
 * Thread-safe storage for [LogEntry] items backed by [InMemoryPluginStorage].
 */
internal class LogStorage(
    capacity: Int = 500,
) {
    private val storage = InMemoryPluginStorage<LogEntry>(capacity)

    /** Hot stream of all recorded log entries. */
    val entries: StateFlow<List<LogEntry>> = storage.dataFlow

    fun record(entry: LogEntry): Unit = storage.add(entry)

    fun clear(): Unit = storage.clear()
}
