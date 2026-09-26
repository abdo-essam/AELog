package com.ae.log.storage

import com.ae.log.utils.FileOperations
import com.ae.log.utils.createFileOperations
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * File-backed [PluginStorage] that survives app restarts.
 *
 * [dataFlow] is the single source of truth at runtime.
 * Disk writes are dispatched asynchronously off the calling thread.
 *
 * @param directoryPath absolute path to the storage directory.
 * @param serializer kotlinx.serialization serializer for [T].
 */
public class PersistentPluginStorage<T>(
    private val directoryPath: String,
    private val serializer: KSerializer<T>,
    private val fileOps: FileOperations = createFileOperations(directoryPath),
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
) : PluginStorage<T> {
    private val lock = SynchronizedObject()
    private val json = Json { ignoreUnknownKeys = true }
    private val _dataFlow = MutableStateFlow<List<T>>(emptyList())
    override val dataFlow: StateFlow<List<T>> = _dataFlow.asStateFlow()

    init {
        fileOps.ensureDirectoryExists()
        _dataFlow.value = loadFromDisk()
    }

    override fun add(item: T) {
        val content =
            synchronized(lock) {
                val encoded = json.encodeToString(serializer, item)
                _dataFlow.value += item
                encoded
            }
        backgroundScope.launch {
            fileOps.writeFile(content)
        }
    }

    override fun clear() {
        synchronized(lock) {
            _dataFlow.value = emptyList()
        }
        backgroundScope.launch {
            fileOps.deleteAllFiles()
        }
    }

    private fun loadFromDisk(): List<T> =
        fileOps.readAllFiles().mapNotNull { content ->
            runCatching { json.decodeFromString(serializer, content) }.getOrNull()
        }
}
