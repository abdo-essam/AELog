package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Details of a foreign key constraint in a table schema.
 *
 * @property id Constraint sequence ID.
 * @property fromColumn Column in this table that holds the foreign key.
 * @property targetTable Referenced table.
 * @property targetColumn Referenced column in the target table.
 * @property onUpdate ON UPDATE action (e.g. `"NO ACTION"`, `"CASCADE"`, `"SET NULL"`).
 * @property onDelete ON DELETE action (e.g. `"NO ACTION"`, `"CASCADE"`, `"SET NULL"`).
 */
@Serializable
public data class TableForeignKey(
    public val id: Int = 0,
    public val fromColumn: String = "",
    public val targetTable: String = "",
    public val targetColumn: String = "",
    public val onUpdate: String = "NO ACTION",
    public val onDelete: String = "NO ACTION",
)
