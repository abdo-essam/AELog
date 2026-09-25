package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Information about a database discovered or registered with the AELog Database plugin.
 *
 * Designed around database engines (e.g. SQLite, SQLCipher, KeyValue, Realm) rather than
 * client libraries/ORMs (Room, SQLDelight, Exposed) which are abstraction layers over engines.
 *
 * @property name Human-readable name of the database (e.g. `"app_database.db"`).
 * @property path Absolute path on the host device or virtual storage identifier.
 * @property isEncrypted `true` if the database is detected as encrypted (e.g. SQLCipher).
 * @property sizeBytes Size of the database file on disk, or `-1` if unavailable.
 * @property engine Database engine (e.g. "SQLite", "SQLCipher", "Realm", "KeyValue").
 * @property framework Optional client library/ORM detected (e.g. "Room", "SQLDelight").
 * @property tableCount Total tables discovered in the database, or `-1` if unknown.
 * @property version Engine or schema version string (e.g. "3.42.0").
 */
@Serializable
public data class DbInfo(
    public val name: String,
    public val path: String,
    public val isEncrypted: Boolean = false,
    public val sizeBytes: Long = -1L,
    public val engine: String = "SQLite",
    public val framework: String? = null,
    public val tableCount: Int = -1,
    public val version: String = "",
)
