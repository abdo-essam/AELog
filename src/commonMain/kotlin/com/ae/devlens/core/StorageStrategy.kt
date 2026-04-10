package com.ae.devlens.core

/**
 * Defines how log entries are stored.
 */
sealed class StorageStrategy {
    /**
     * Fast, RAM-only storage. Logs are lost on app restart.
     * Best for general-purpose logging.
     */
    data class InMemory(val maxEntries: Int = 500) : StorageStrategy()

    /**
     * Hybrid mode: recent logs in memory, crashes persisted to file.
     * Crash logs survive app restarts and can be retrieved on next launch.
     */
    data class Hybrid(
        val memoryMaxEntries: Int = 500,
        val persistCrashesOnly: Boolean = true,
        val retentionDays: Int = 7
    ) : StorageStrategy()
}
