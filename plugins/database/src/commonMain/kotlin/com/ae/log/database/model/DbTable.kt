package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Metadata and schema details for a single database table.
 *
 * @property name Name of the table.
 * @property rowCount Total row count, or `-1` if not yet counted.
 * @property columns Column names in declaration order.
 * @property isSystemTable `true` if this is an internal database system table (e.g. `sqlite_master`, `android_metadata`).
 * @property sizeBytes Approximate or calculated size in bytes, or `-1` if unavailable.
 */
@Serializable
public data class DbTable(
    public val name: String,
    public val rowCount: Long = -1L,
    public val columns: List<String> = emptyList(),
    public val isSystemTable: Boolean = false,
    public val sizeBytes: Long = -1L,
)
