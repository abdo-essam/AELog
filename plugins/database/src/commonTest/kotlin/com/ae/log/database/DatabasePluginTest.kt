package com.ae.log.database

import com.ae.log.AELog
import com.ae.log.AELogTestApi
import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.inspector.DatabaseInspector
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(AELogTestApi::class)
class DatabasePluginTest {
    private class FakeDatabaseInspector : DatabaseInspector {
        val databases =
            mutableListOf(
                DbInfo(name = "test.db", path = "/data/test.db", isEncrypted = false),
                DbInfo(name = "secure.db", path = "/data/secure.db", isEncrypted = true),
            )

        val tables =
            mutableListOf(
                DbTable(name = "users", rowCount = 2L, columns = listOf("id", "name")),
                DbTable(name = "sqlite_sequence", rowCount = 1L, columns = listOf("name", "seq"), isSystemTable = true),
            )

        override fun listDatabases(): List<DbInfo> = databases

        override fun listTables(dbInfo: DbInfo): List<DbTable> = tables

        override fun query(
            dbInfo: DbInfo,
            sql: String,
            args: List<String>,
            allowWrite: Boolean,
        ): QueryResult {
            if (sql.startsWith("SELECT * FROM \"users\"")) {
                return QueryResult.success(
                    columns = listOf("id", "name"),
                    rows =
                        listOf(
                            listOf("1", "Alice"),
                            listOf("2", "Bob"),
                        ),
                )
            }
            if (sql.startsWith("DELETE FROM users")) {
                if (!allowWrite) return QueryResult.error("Write disallowed")
                return QueryResult.writeSuccess(affectedRows = 2L)
            }
            return QueryResult.success(columns = listOf("val"), rows = listOf(listOf("ok")))
        }

        override fun registerDatabase(dbInfo: DbInfo) {
            databases.add(dbInfo)
        }
    }

    @BeforeTest
    fun setup() {
        AELog.resetForTesting()
    }

    @AfterTest
    fun tearDown() {
        AELog.resetForTesting()
    }

    @Test
    fun databasePlugin_registersAndProvidesProxyApi() {
        val fakeInspector = FakeDatabaseInspector()
        val plugin =
            DatabasePlugin(
                config = DatabasePluginConfig(allowWrite = false),
                inspector = fakeInspector,
            )

        AELog.install(plugin)

        val retrievedPlugin = AELog.getPlugin<DatabasePlugin>()
        assertNotNull(retrievedPlugin)
        assertEquals("Database", retrievedPlugin.name)
        assertEquals(DatabasePlugin.ID, retrievedPlugin.id)

        val databases = AELog.database.listDatabases()
        assertEquals(2, databases.size)
        assertEquals("test.db", databases[0].name)
        assertEquals("secure.db", databases[1].name)
        assertTrue(databases[1].isEncrypted)
    }

    @Test
    fun databasePlugin_listsTablesAndRunsQueries() {
        val fakeInspector = FakeDatabaseInspector()
        val plugin =
            DatabasePlugin(
                config = DatabasePluginConfig(allowWrite = false),
                inspector = fakeInspector,
            )
        AELog.install(plugin)

        val tables = AELog.database.listTables("test.db")
        assertEquals(2, tables.size)
        assertEquals("users", tables[0].name)

        val queryResult = AELog.database.query("test.db", "SELECT * FROM \"users\" LIMIT 50 OFFSET 0")
        assertTrue(queryResult.isSuccess)
        assertEquals(listOf("id", "name"), queryResult.columns)
        assertEquals(2, queryResult.rows.size)
        assertEquals(listOf("1", "Alice"), queryResult.rows[0])
    }

    @Test
    fun databasePlugin_export_containsDatabasesAndTables() {
        val fakeInspector = FakeDatabaseInspector()
        val plugin =
            DatabasePlugin(
                config = DatabasePluginConfig(allowWrite = false),
                inspector = fakeInspector,
            )
        AELog.install(plugin)

        val exportText = plugin.export()
        assertTrue(exportText.contains("test.db"))
        assertTrue(exportText.contains("secure.db"))
        assertTrue(exportText.contains("users"))
    }
}
