package com.ae.log.utils

/**
 * Platform-independent interface for file operations used by persistence layers.
 *
 * Implementations backed by a real filesystem should override [ensureDirectoryExists]
 * to create the storage directory before first use.  In-memory or browser-based
 * implementations may rely on the default no-op — there is no directory to create.
 */
public interface FileOperations {
    /**
     * Ensures the storage directory exists before any read/write operations.
     *
     * Default implementation is a no-op, suitable for in-memory implementations
     * (e.g. [WasmJsFileOperations]) where the concept of a directory does not apply.
     * Filesystem-backed platforms (JVM, Android, iOS) should override this.
     */
    public fun ensureDirectoryExists(): Unit = Unit

    public fun writeFile(content: String)

    public fun readAllFiles(): List<String>

    public fun deleteAllFiles()
}

/**
 * Platform-specific factory function to create a [FileOperations] instance.
 */
public expect fun createFileOperations(directoryPath: String): FileOperations
