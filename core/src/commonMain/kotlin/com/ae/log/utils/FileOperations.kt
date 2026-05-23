package com.ae.log.utils

/**
 * Platform-independent interface for file operations used by persistence layers.
 */
public interface FileOperations {
    public fun ensureDirectoryExists()

    public fun writeFile(content: String)

    public fun readAllFiles(): List<String>

    public fun deleteAllFiles()
}

/**
 * Platform-specific factory function to create a [FileOperations] instance.
 */
public expect fun createFileOperations(directoryPath: String): FileOperations
