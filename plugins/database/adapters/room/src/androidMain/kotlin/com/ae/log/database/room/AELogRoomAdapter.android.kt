package com.ae.log.database.room

import androidx.room.RoomDatabase
import com.ae.log.AELog
import com.ae.log.database.database
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

/**
 * Room query callback that automatically forwards all database queries executed by Room
 * (including SELECT, INSERT, UPDATE, DELETE, and migrations) to AELog Database Logs on Android.
 *
 * ### Usage:
 * ```kotlin
 * Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
 *     .setQueryCallback(AELogRoomQueryCallback("app.db"), Executors.newSingleThreadExecutor())
 *     .build()
 * ```
 * Or use the fluent extension:
 * ```kotlin
 * Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
 *     .addAELogCallback("app.db")
 *     .build()
 * ```
 */
public class AELogRoomQueryCallback(
    private val databaseName: String,
) : RoomDatabase.QueryCallback {
    override fun onQuery(
        sqlQuery: String,
        bindArgs: List<Any?>,
    ) {
        AELog.database.logQuery(
            databaseName = databaseName,
            sql = sqlQuery,
            durationMs = 0L,
            bindArgs = bindArgs,
        )
    }
}

/**
 * Convenient alias for [AELogRoomQueryCallback].
 */
public typealias AELogRoomCallback = AELogRoomQueryCallback

private const val DEFAULT_DB_NAME = "app.db"

/**
 * Extension on [RoomDatabase.Builder] to attach AELog query logging in a single line on Android.
 *
 * ### Usage:
 * ```kotlin
 * val db = Room.databaseBuilder(context, AppDatabase::class.java, "app.db")
 *     .addAELogCallback("app.db")
 *     .build()
 * ```
 */
public fun <T : RoomDatabase> RoomDatabase.Builder<T>.addAELogCallback(
    databaseName: String = DEFAULT_DB_NAME,
    executor: Executor = Executors.newSingleThreadExecutor(),
): RoomDatabase.Builder<T> = setQueryCallback(AELogRoomQueryCallback(databaseName), executor)

/**
 * Extension on [RoomDatabase.Builder] using coroutine context to attach AELog query logging on Android.
 */
public fun <T : RoomDatabase> RoomDatabase.Builder<T>.addAELogCallback(
    context: CoroutineContext,
    databaseName: String = DEFAULT_DB_NAME,
): RoomDatabase.Builder<T> = setQueryCallback(context, AELogRoomQueryCallback(databaseName))

/**
 * Backward-compatible alias for [addAELogCallback].
 */
public fun <T : RoomDatabase> RoomDatabase.Builder<T>.addAELogInterceptor(
    databaseName: String = DEFAULT_DB_NAME,
    executor: Executor = Executors.newSingleThreadExecutor(),
): RoomDatabase.Builder<T> = addAELogCallback(databaseName, executor)
