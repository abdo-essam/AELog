package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Details of an index on a database table.
 *
 * @property name Index name.
 * @property isUnique Whether this index enforces uniqueness.
 * @property columns Ordered column names participating in the index.
 */
@Serializable
public data class TableIndex(
    public val name: String,
    public val isUnique: Boolean = false,
    public val columns: List<String> = emptyList(),
)
