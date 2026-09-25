package com.ae.log.database.room

import androidx.room.RoomDatabase
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import com.ae.log.AELog
import com.ae.log.database.database

private const val DEFAULT_DB_NAME = "app.db"

/**
 * Extension on [RoomDatabase.Builder] to attach an AELog-monitored [SQLiteDriver].
 *
 * This works across all multiplatform targets supported by Room (Android, iOS, JVM).
 *
 * ### Usage:
 * ```kotlin
 * Room.databaseBuilder<AppDatabase>(name = dbFilePath)
 *     .setAELogDriver(BundledSQLiteDriver(), databaseName = "app.db")
 *     .build()
 * ```
 */
public fun <T : RoomDatabase> RoomDatabase.Builder<T>.setAELogDriver(
    driver: SQLiteDriver,
    databaseName: String = DEFAULT_DB_NAME,
): RoomDatabase.Builder<T> = setDriver(AELogSQLiteDriver(driver, databaseName))

/**
 * Wraps this [SQLiteDriver] with AELog query logging.
 */
public fun SQLiteDriver.withAELog(databaseName: String = DEFAULT_DB_NAME): SQLiteDriver =
    AELogSQLiteDriver(this, databaseName)

/**
 * A delegating [SQLiteDriver] that intercepts and logs all executed SQL statements to AELog Database Logs.
 */
public class AELogSQLiteDriver(
    private val delegate: SQLiteDriver,
    private val databaseName: String = DEFAULT_DB_NAME,
) : SQLiteDriver {
    override fun open(fileName: String): SQLiteConnection {
        val resolvedName =
            if (databaseName == DEFAULT_DB_NAME) {
                fileName.substringAfterLast('/').substringAfterLast('\\').ifBlank { databaseName }
            } else {
                databaseName
            }
        return AELogSQLiteConnection(delegate.open(fileName), resolvedName)
    }
}

/**
 * A delegating [SQLiteConnection] that wraps prepared statements for AELog query logging.
 */
public class AELogSQLiteConnection(
    private val delegate: SQLiteConnection,
    private val databaseName: String,
) : SQLiteConnection by delegate {
    override fun prepare(sql: String): SQLiteStatement {
        val statement = delegate.prepare(sql)
        return AELogSQLiteStatement(statement, databaseName, sql)
    }
}

/**
 * A delegating [SQLiteStatement] that records query parameters and execution to AELog.
 */
public class AELogSQLiteStatement(
    private val delegate: SQLiteStatement,
    private val databaseName: String,
    private val sql: String,
) : SQLiteStatement by delegate {
    private val boundArgs = mutableMapOf<Int, Any?>()
    private var hasExecuted = false

    override fun bindBlob(
        index: Int,
        value: ByteArray,
    ) {
        boundArgs[index] = value
        delegate.bindBlob(index, value)
    }

    override fun bindDouble(
        index: Int,
        value: Double,
    ) {
        boundArgs[index] = value
        delegate.bindDouble(index, value)
    }

    override fun bindFloat(
        index: Int,
        value: Float,
    ) {
        boundArgs[index] = value
        delegate.bindFloat(index, value)
    }

    override fun bindLong(
        index: Int,
        value: Long,
    ) {
        boundArgs[index] = value
        delegate.bindLong(index, value)
    }

    override fun bindInt(
        index: Int,
        value: Int,
    ) {
        boundArgs[index] = value
        delegate.bindInt(index, value)
    }

    override fun bindBoolean(
        index: Int,
        value: Boolean,
    ) {
        boundArgs[index] = value
        delegate.bindBoolean(index, value)
    }

    override fun bindText(
        index: Int,
        value: String,
    ) {
        boundArgs[index] = value
        delegate.bindText(index, value)
    }

    override fun bindNull(index: Int) {
        boundArgs[index] = null
        delegate.bindNull(index)
    }

    override fun step(): Boolean {
        if (!hasExecuted) {
            hasExecuted = true
            logQuery()
        }
        return delegate.step()
    }

    private fun logQuery() {
        val lowerSql = sql.lowercase()
        if (lowerSql.contains("room_table_modification_log") ||
            lowerSql.contains("room_master_table") ||
            lowerSql.contains("sqlite_master") ||
            lowerSql.contains("sqlite_schema") ||
            lowerSql.contains("sqlite_sequence")
        ) {
            return
        }

        val argsList =
            if (boundArgs.isEmpty()) {
                emptyList()
            } else {
                val maxIndex = boundArgs.keys.maxOrNull() ?: 0
                (1..maxIndex).map { boundArgs[it] }
            }
        AELog.database.logQuery(
            databaseName = databaseName,
            sql = sql,
            durationMs = 0L,
            bindArgs = argsList,
        )
    }

    override fun reset() {
        hasExecuted = false
        delegate.reset()
    }

    override fun clearBindings() {
        boundArgs.clear()
        delegate.clearBindings()
    }
}
