package com.ae.log.database.sqlite

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.SQLiteDriver
import androidx.sqlite.SQLiteStatement
import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.database.DatabaseLogRecorder
import com.ae.log.database.DatabasePlugin
import com.ae.log.database.config.DatabasePluginConfig
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TEST_DB_NAME = "test.db"
private const val TEST_QUERY = "SELECT * FROM users WHERE id = ?"
private const val DB_FILE_PATH = "/path/to/test.db"

@OptIn(AELogTestApi::class)
class AELogSQLiteDriverTest {
    @BeforeTest
    fun setUp() {
        AELog.resetForTesting()
        val plugin = DatabasePlugin(DatabasePluginConfig())
        AELog.install(plugin)
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    @Test
    fun driverLogsQueryOnStep() {
        var openCalled = false
        var prepareCalled = false
        var stepCalled = false

        val fakeStatement =
            object : SQLiteStatement {
                override fun bindBlob(
                    index: Int,
                    value: ByteArray,
                ) {}

                override fun bindDouble(
                    index: Int,
                    value: Double,
                ) {}

                override fun bindLong(
                    index: Int,
                    value: Long,
                ) {}

                override fun bindText(
                    index: Int,
                    value: String,
                ) {}

                override fun bindNull(index: Int) {}

                override fun getBlob(index: Int): ByteArray = ByteArray(0)

                override fun getDouble(index: Int): Double = 0.0

                override fun getLong(index: Int): Long = 0L

                override fun getText(index: Int): String = ""

                override fun isNull(index: Int): Boolean = false

                override fun getColumnCount(): Int = 0

                override fun getColumnName(index: Int): String = ""

                override fun getColumnType(index: Int): Int = 0

                override fun step(): Boolean {
                    stepCalled = true
                    return false
                }

                override fun reset() {}

                override fun clearBindings() {}

                override fun close() {}
            }

        val fakeConnection =
            object : SQLiteConnection {
                override fun prepare(sql: String): SQLiteStatement {
                    prepareCalled = true
                    return fakeStatement
                }

                override fun close() {}
            }

        val fakeDriver =
            object : SQLiteDriver {
                override fun open(fileName: String): SQLiteConnection {
                    openCalled = true
                    return fakeConnection
                }
            }

        val driver = AELogSQLiteDriver(fakeDriver, TEST_DB_NAME)
        val connection = driver.open(DB_FILE_PATH)
        assertTrue(openCalled)

        val statement = connection.prepare(TEST_QUERY)
        assertTrue(prepareCalled)

        statement.bindLong(1, 42L)
        statement.step()
        assertTrue(stepCalled)

        val logs =
            AELog
                .getPlugin<DatabasePlugin>()
                ?.logRecorder
                ?.logs
                ?.value
                ?: DatabaseLogRecorder.defaultInstance.logs.value
        assertEquals(1, logs.size)
        val entry = logs.first()
        assertEquals(TEST_DB_NAME, entry.databaseName)
        assertTrue(entry.sql.startsWith(TEST_QUERY))
        assertTrue(entry.sql.contains("42"))
        assertTrue(entry.durationMs >= 0L)
    }
}
