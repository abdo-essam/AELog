package com.ae.log.utils

/**
 * Wasm/JS implementation of [FileOperations].
 *
 * Browsers have no writable file-system API accessible from Kotlin/Wasm without
 * additional JS setup.  All data is held in memory for the lifetime of the page.
 * This is sufficient for AELog's in-session persistence needs; tabs that are
 * closed or refreshed will start with an empty store.
 */
internal class WasmJsFileOperations : FileOperations {
    private val entries = mutableListOf<String>()

    // ensureDirectoryExists() intentionally not overridden — the interface default
    // no-op is correct for an in-memory store with no physical directory.

    override fun writeFile(content: String) {
        entries.add(content)
    }

    override fun readAllFiles(): List<String> = entries.toList()

    override fun deleteAllFiles() {
        entries.clear()
    }
}

/**
 * Returns a Wasm/JS [FileOperations] backed by an in-memory list.
 *
 * The [directoryPath] parameter is accepted for API compatibility but is
 * otherwise unused — there is no real directory in a browser context.
 */
public actual fun createFileOperations(directoryPath: String): FileOperations = WasmJsFileOperations()
