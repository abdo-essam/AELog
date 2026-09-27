package com.ae.log.database.inspector

public const val DEFAULT_DB_NAME: String = "app.db"
public const val SQLITE_HEADER_PREFIX: String = "SQLite format 3"

/**
 * Checks if the file name represents an auxiliary SQLite file (journal, WAL, SHM, locks, or backups).
 */
public fun isAuxiliaryFile(name: String): Boolean {
    val lower = name.lowercase()
    return lower.endsWith("-wal") ||
        lower.endsWith(".wal") ||
        lower.endsWith("-shm") ||
        lower.endsWith(".shm") ||
        lower.endsWith("-journal") ||
        lower.endsWith(".journal") ||
        lower.endsWith("-lck") ||
        lower.endsWith(".lck") ||
        lower.endsWith("-lock") ||
        lower.endsWith(".lock") ||
        lower.endsWith("-tmp") ||
        lower.endsWith(".tmp") ||
        lower.endsWith("-bak") ||
        lower.endsWith(".bak")
}

/**
 * Checks whether a given filename has a standard SQLite database extension (.db, .sqlite, .sqlite3).
 */
public fun isSqliteFileName(name: String): Boolean {
    val lower = name.lowercase()
    return lower.endsWith(".db") || lower.endsWith(".sqlite") || lower.endsWith(".sqlite3")
}

/**
 * Detects whether the 16-byte SQLite file header indicates encryption (e.g. SQLCipher).
 * A standard SQLite database starts with the ASCII header "SQLite format 3".
 */
public fun isEncryptedSqliteHeader(header: ByteArray): Boolean {
    if (header.size < 16) return false
    val prefix = header.take(15).map { it.toInt().toChar() }.joinToString("")
    return !prefix.startsWith(SQLITE_HEADER_PREFIX)
}
