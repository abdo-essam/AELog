package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Details of a single database column in a table schema.
 *
 * @property name Column name.
 * @property type SQL data type (e.g. `"INTEGER"`, `"TEXT"`, `"REAL"`, `"BLOB"`).
 * @property isPrimaryKey Whether this column is a primary key.
 * @property isNotNull Whether this column has a NOT NULL constraint.
 * @property defaultValue Default value expression, or null if none.
 */
@Serializable
public data class TableColumn(
    public val name: String,
    public val type: String = "TEXT",
    public val isPrimaryKey: Boolean = false,
    public val isNotNull: Boolean = false,
    public val defaultValue: String? = null,
)
