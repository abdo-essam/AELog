package com.ae.log.crashes

/**
 * Wasm/JS actual for [defaultCrashStorageDir].
 *
 * In a browser environment there is no persistent file-system path.
 * The crashes plugin uses an in-memory [FileOperations] implementation on
 * this platform, so the directory path is a logical key only and is never
 * mapped to the real filesystem.
 */
internal actual fun defaultCrashStorageDir(): String = "ae_crashes"
