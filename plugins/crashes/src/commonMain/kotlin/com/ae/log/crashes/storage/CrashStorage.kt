package com.ae.log.crashes.storage

import com.ae.log.crashes.model.CrashEvent
import com.ae.log.storage.PersistentPluginStorage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.serializer

/**
 * File-backed storage for [CrashEvent] items.
 *
 * Survives app restarts — crashes are persisted to disk immediately on capture
 * and reloaded on next launch, ensuring no event is silently lost.
 *
 * Delegates all mutation and I/O to [PersistentPluginStorage].
 */
internal class CrashStorage(
    directoryPath: String,
) {
    private val storage =
        PersistentPluginStorage(
            directoryPath = directoryPath,
            serializer = serializer<CrashEvent>(),
        )

    val events: StateFlow<List<CrashEvent>> = storage.dataFlow

    fun record(event: CrashEvent): Unit = storage.add(event)

    fun clear(): Unit = storage.clear()
}
