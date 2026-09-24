package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Information about a database discovered or registered with the AELog Database plugin.
 *
 * @property name Human-readable name of the database (e.g. `"app_database.db"`).
 * @property path Absolute path on the host device or virtual storage identifier.
 * @property isEncrypted `true` if the database is detected as encrypted (e.g. SQLCipher).
 * @property sizeBytes Size of the database file on disk, or `-1` if unavailable.
 * @property engine Database engine/wrapper (e.g. "Room", "SQLDelight", "SQLite").
 * @property tableCount Total tables discovered in the database, or `-1` if unknown.
 * @property version SQLite or schema version string.
 */
@Serializable
public data class DbInfo(
    public val name: String,
    public val path: String,
    public val isEncrypted: Boolean = false,
    public val sizeBytes: Long = -1L,
    public val engine: String = "SQLite",
    public val tableCount: Int = -1,
    public val version: String = "",
)
