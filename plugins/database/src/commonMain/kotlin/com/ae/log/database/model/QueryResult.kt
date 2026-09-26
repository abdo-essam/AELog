package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Result of executing a SQL statement against a database.
 *
 * @property columns Ordered column headers for tabular results.
 * @property rows Grid of row values serialized as strings (null for SQL NULL).
 * @property affectedRows Number of rows affected by write statements (INSERT, UPDATE, DELETE), or `null` for queries.
 * @property executionDurationMs Execution time in milliseconds.
 * @property errorMessage Non-null if the query failed to execute.
 */
@Serializable
public data class QueryResult(
    public val columns: List<String> = emptyList(),
    public val rows: List<List<String?>> = emptyList(),
    public val affectedRows: Long? = null,
    public val executionDurationMs: Long = 0L,
    public val errorMessage: String? = null,
) {
    public val isSuccess: Boolean get() = errorMessage == null

    public companion object {
        public fun error(
            message: String,
            durationMs: Long = 0L,
        ): QueryResult =
            QueryResult(
                errorMessage = message,
                executionDurationMs = durationMs,
            )

        public fun success(
            columns: List<String>,
            rows: List<List<String?>>,
            durationMs: Long = 0L,
        ): QueryResult =
            QueryResult(
                columns = columns,
                rows = rows,
                executionDurationMs = durationMs,
            )

        public fun writeSuccess(
            affectedRows: Long,
            durationMs: Long = 0L,
        ): QueryResult =
            QueryResult(
                affectedRows = affectedRows,
                executionDurationMs = durationMs,
            )
    }
}
