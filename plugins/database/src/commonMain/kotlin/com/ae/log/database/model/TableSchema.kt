package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Full schema representation for a database table including columns, indexes, and foreign keys.
 *
 * @property tableName Name of the table.
 * @property columns Ordered column definitions.
 * @property indexes Defined indexes on the table.
 * @property foreignKeys Defined foreign key constraints on the table.
 */
@Serializable
public data class TableSchema(
    public val tableName: String,
    public val columns: List<TableColumn> = emptyList(),
    public val indexes: List<TableIndex> = emptyList(),
    public val foreignKeys: List<TableForeignKey> = emptyList(),
)
