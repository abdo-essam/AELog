package com.ae.log.storage

import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * File-backed [PluginStorage] that survives app restarts.
 *
 * [dataFlow] is the single source of truth at runtime.
 * Disk writes are a side effect of mutations.
 *
 * @param directoryPath absolute path to the storage directory.
 * @param serializer kotlinx.serialization serializer for [T].
 */
public class PersistentPluginStorage<T>(
    private val directoryPath: String,
    private val serializer: KSerializer<T>,
) : PluginStorage<T> {

    private val lock = SynchronizedObject()
    private val json = Json { ignoreUnknownKeys = true }
    private val fileOps = FileOperations(directoryPath)
    private val _dataFlow = MutableStateFlow<List<T>>(emptyList())
    override val dataFlow: StateFlow<List<T>> = _dataFlow.asStateFlow()

    init {
        fileOps.ensureDirectoryExists()
        _dataFlow.value = loadFromDisk()
    }

    override fun add(item: T) {
        synchronized(lock) {
            val content = json.encodeToString(serializer, item)
            fileOps.writeFile(content)
            _dataFlow.value += item
        }
    }

    override fun clear() {
        synchronized(lock) {
            fileOps.deleteAllFiles()
            _dataFlow.value = emptyList()
        }
    }

    private fun loadFromDisk(): List<T> =
        fileOps.readAllFiles().mapNotNull { content ->
            runCatching { json.decodeFromString(serializer, content) }.getOrNull()
        }
}
