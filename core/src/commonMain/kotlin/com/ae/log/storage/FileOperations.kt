package com.ae.log.storage

/**
 * Platform-specific file operations for [PersistentPluginStorage].
 *
 * Each platform provides its own implementation via expect/actual.
 */
internal expect class FileOperations(
    directoryPath: String,
) {
    fun ensureDirectoryExists()

    fun writeFile(content: String)

    fun readAllFiles(): List<String>

    fun deleteAllFiles()
}
