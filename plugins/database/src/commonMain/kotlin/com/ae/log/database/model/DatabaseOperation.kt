package com.ae.log.database.model

import kotlinx.serialization.Serializable

/**
 * Classification of database SQL operations.
 */
@Serializable
public enum class DatabaseOperation(
    public val label: String,
) {
    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE"),
    CREATE("CREATE"),
    DROP("DROP"),
    ALTER("ALTER"),
    REPLACE("REPLACE"),
    PRAGMA("PRAGMA"),
    TRANSACTION("TRANSACTION"),
    ERROR("ERROR"),
    OTHER("OTHER");

    public companion object {
        /**
         * Parses a raw SQL operation string into a [DatabaseOperation].
         */
        public fun parse(raw: String?): DatabaseOperation {
            if (raw.isNullOrBlank()) return OTHER
            val upper = raw.trim().uppercase()
            return entries.find { it.name == upper } ?: OTHER
        }
    }
}
