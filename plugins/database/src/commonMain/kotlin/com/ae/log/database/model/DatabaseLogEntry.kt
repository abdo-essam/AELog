package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Log record of a single database query or operation.
 *
 * @property id Unique identifier for the log entry.
 * @property databaseName The target database name.
 * @property tableName The table operated on, if identifiable.
 * @property sql The SQL statement executed.
 * @property operation Operation classification: `"SELECT"`, `"INSERT"`, `"UPDATE"`, `"DELETE"`, `"ERROR"`, or `"OTHER"`.
 * @property durationMs Execution time in milliseconds.
 * @property timestamp Epoch timestamp in milliseconds.
 * @property isSuccess Whether the query succeeded without error.
 * @property errorMessage Error details if [isSuccess] is `false`.
 * @property affectedRows Number of affected rows for write statements.
 */
@Serializable
public data class DatabaseLogEntry(
    public val id: String,
    public val databaseName: String,
    public val tableName: String? = null,
    public val sql: String,
    public val operation: String = "SELECT",
    public val durationMs: Long = 0L,
    public val timestamp: Long = 0L,
    public val isSuccess: Boolean = true,
    public val errorMessage: String? = null,
    public val affectedRows: Long? = null,
)

/**
 * Filter categories for database log entries.
 */
public enum class DatabaseLogFilter(public val label: String) {
    ALL("All"),
    QUERIES("Queries"),
    WRITES("Writes"),
    ERRORS("Errors"),
}
