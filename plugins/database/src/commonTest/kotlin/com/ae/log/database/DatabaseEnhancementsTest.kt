package com.ae.log.database

import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.inspector.DatabaseInspector
import com.ae.log.database.model.DatabaseLogFilter
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import com.ae.log.database.ui.DatabaseDestination
import com.ae.log.database.ui.DatabaseFormatUtils
import com.ae.log.database.ui.DatabaseViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseEnhancementsTest {
    private class TestDatabaseInspector : DatabaseInspector {
        val databases =
            listOf(
                DbInfo(
                    name = "shop.db",
                    path = "/app/shop.db",
                    engine = "Room",
                    tableCount = 3,
                    sizeBytes = 12400000L,
                ),
            )

        val tables =
            listOf(
                DbTable(name = "products", rowCount = 124L, columns = listOf("id", "name", "price", "stock")),
                DbTable(name = "users", rowCount = 12L, columns = listOf("id", "email")),
            )

        override fun listDatabases(): List<DbInfo> = databases

        override fun listTables(dbInfo: DbInfo): List<DbTable> = tables

        override fun query(
            dbInfo: DbInfo,
            sql: String,
            args: List<String>,
            allowWrite: Boolean,
        ): QueryResult {
            if (sql.contains("PRAGMA table_info(\"products\")")) {
                return QueryResult.success(
                    columns = listOf("cid", "name", "type", "notnull", "dflt_value", "pk"),
                    rows =
                        listOf(
                            listOf("0", "id", "INTEGER", "1", null, "1"),
                            listOf("1", "name", "TEXT", "1", null, "0"),
                            listOf("2", "price", "REAL", "1", null, "0"),
                            listOf("3", "stock", "INTEGER", "1", null, "0"),
                        ),
                )
            }
            if (sql.contains("PRAGMA index_list(\"products\")")) {
                return QueryResult.success(
                    columns = listOf("seq", "name", "unique", "origin", "partial"),
                    rows =
                        listOf(
                            listOf("0", "sqlite_autoindex_products_1", "1", "u", "0"),
                        ),
                )
            }
            if (sql.contains("PRAGMA index_info(\"sqlite_autoindex_products_1\")")) {
                return QueryResult.success(
                    columns = listOf("seqno", "cid", "name"),
                    rows =
                        listOf(
                            listOf("0", "1", "name"),
                        ),
                )
            }
            if (sql.contains("PRAGMA foreign_key_list(\"products\")")) {
                return QueryResult.success(
                    columns = listOf("id", "seq", "table", "from", "to", "on_update", "on_delete", "match"),
                    rows =
                        listOf(
                            listOf("0", "0", "categories", "category_id", "id", "NO ACTION", "CASCADE", "NONE"),
                        ),
                )
            }
            if (sql.startsWith("SELECT * FROM \"products\"")) {
                return QueryResult.success(
                    columns = listOf("id", "name", "price", "stock"),
                    rows =
                        listOf(
                            listOf("1", "Chair", "500.0", "12"),
                            listOf("2", "Table", "1200.0", "4"),
                        ),
                )
            }
            return QueryResult.success(columns = emptyList(), rows = emptyList())
        }
    }

    @BeforeTest
    fun setup() {
        DatabaseLogRecorder.clear()
    }

    @AfterTest
    fun tearDown() {
        DatabaseLogRecorder.clear()
    }

    @Test
    fun databaseFormatUtils_formatsRowToJson() {
        val row =
            mapOf(
                "id" to "1",
                "name" to "Chair",
                "price" to "500.0",
                "stock" to "12",
            )
        val json = DatabaseFormatUtils.rowToJson(row)
        assertTrue(json.contains("\"id\": 1"))
        assertTrue(json.contains("\"name\": \"Chair\""))
        assertTrue(json.contains("\"price\": 500.0"))
        assertTrue(json.contains("\"stock\": 12"))
    }

    @Test
    fun databaseFormatUtils_formatsRowToSqlInsert() {
        val row =
            mapOf(
                "id" to "1",
                "name" to "Chair",
                "price" to "500.0",
            )
        val sql = DatabaseFormatUtils.rowToSqlInsert("products", row)
        assertEquals("INSERT INTO \"products\" (\"id\", \"name\", \"price\") VALUES (1, 'Chair', 500.0);", sql)
    }

    @Test
    fun databaseFormatUtils_formatsBytes() {
        assertEquals("0 B", DatabaseFormatUtils.formatBytes(0))
        assertEquals("500 B", DatabaseFormatUtils.formatBytes(500))
        assertEquals("10.0 KB", DatabaseFormatUtils.formatBytes(10240))
        assertEquals("12.4 MB", DatabaseFormatUtils.formatBytes(13002342))
    }

    @Test
    fun databaseLogRecorder_recordsAndFilters() {
        DatabaseLogRecorder.record(
            databaseName = "shop.db",
            sql = "SELECT * FROM products WHERE stock > 0",
            durationMs = 4L,
        )
        DatabaseLogRecorder.record(
            databaseName = "shop.db",
            sql = "INSERT INTO orders VALUES (1, 100)",
            durationMs = 12L,
        )
        DatabaseLogRecorder.record(
            databaseName = "shop.db",
            sql = "DELETE FROM items WHERE id = 5",
            durationMs = 6L,
        )
        DatabaseLogRecorder.record(
            databaseName = "shop.db",
            sql = "SELECT * FROM invalid_table",
            durationMs = 15L,
            isSuccess = false,
            errorMessage = "no such table: invalid_table",
        )

        val logs = DatabaseLogRecorder.logs.value
        assertEquals(4, logs.size)

        // Check operations
        assertEquals("ERROR", logs[0].operation)
        assertEquals("DELETE", logs[1].operation)
        assertEquals("INSERT", logs[2].operation)
        assertEquals("SELECT", logs[3].operation)

        // Test clear
        DatabaseLogRecorder.clear()
        assertTrue(DatabaseLogRecorder.logs.value.isEmpty())
    }

    @Test
    fun databaseInspector_extractsSchemaWithColumnsAndIndexes() {
        val inspector = TestDatabaseInspector()
        val dbInfo = inspector.listDatabases().first()
        val schema = inspector.getSchema(dbInfo, "products")

        assertEquals("products", schema.tableName)
        assertEquals(4, schema.columns.size)

        val idCol = schema.columns.first { it.name == "id" }
        assertEquals("INTEGER", idCol.type)
        assertTrue(idCol.isPrimaryKey)
        assertTrue(idCol.isNotNull)

        val nameCol = schema.columns.first { it.name == "name" }
        assertEquals("TEXT", nameCol.type)
        assertFalse(nameCol.isPrimaryKey)
        assertTrue(nameCol.isNotNull)

        assertEquals(1, schema.indexes.size)
        val idx = schema.indexes.first()
        assertEquals("sqlite_autoindex_products_1", idx.name)
        assertTrue(idx.isUnique)
        assertEquals(listOf("name"), idx.columns)

        assertEquals(1, schema.foreignKeys.size)
        val fk = schema.foreignKeys.first()
        assertEquals("category_id", fk.fromColumn)
        assertEquals("categories", fk.targetTable)
        assertEquals("id", fk.targetColumn)
        assertEquals("CASCADE", fk.onDelete)
    }

    @Test
    fun databaseViewModel_navigationStackOperations() {
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val inspector = TestDatabaseInspector()
        val vm = DatabaseViewModel(inspector, DatabasePluginConfig(), testScope)

        // Starts at DatabaseList
        assertEquals(DatabaseDestination.DatabaseList, vm.currentDestination.value)
        assertEquals(1, vm.navigationStack.value.size)

        // Navigate to TablesList
        val db = inspector.listDatabases().first()
        vm.selectDatabase(db, navigate = true)
        assertEquals(DatabaseDestination.TablesList(db), vm.currentDestination.value)
        assertEquals(2, vm.navigationStack.value.size)

        // Navigate to TableData
        val table = inspector.listTables(db).first()
        vm.selectTable(db, table, navigate = true)
        assertEquals(DatabaseDestination.TableData(db, table), vm.currentDestination.value)
        assertEquals(3, vm.navigationStack.value.size)

        // Navigate to RowDetails
        vm.openRowDetails(db, table, listOf("id", "name"), listOf("1", "Chair"), 0)
        assertTrue(vm.currentDestination.value is DatabaseDestination.RowDetails)
        assertEquals(4, vm.navigationStack.value.size)

        // Pop back to TableData
        assertTrue(vm.popBack())
        assertEquals(DatabaseDestination.TableData(db, table), vm.currentDestination.value)

        // Pop back to TablesList
        assertTrue(vm.popBack())
        assertEquals(DatabaseDestination.TablesList(db), vm.currentDestination.value)

        // Pop back to DatabaseList
        assertTrue(vm.popBack())
        assertEquals(DatabaseDestination.DatabaseList, vm.currentDestination.value)

        // Cannot pop further
        assertFalse(vm.popBack())
        assertEquals(DatabaseDestination.DatabaseList, vm.currentDestination.value)
    }

    @Test
    fun databaseViewModel_logsFiltering_allQueriesWritesErrors() {
        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)
        val inspector = TestDatabaseInspector()
        val vm = DatabaseViewModel(inspector, DatabasePluginConfig(), testScope)

        DatabaseLogRecorder.clear()

        DatabaseLogRecorder.record(
            databaseName = "app.db",
            sql = "SELECT * FROM users",
            durationMs = 2L,
        )
        DatabaseLogRecorder.record(
            databaseName = "app.db",
            sql = "INSERT INTO users (name) VALUES ('Test')",
            durationMs = 5L,
        )
        DatabaseLogRecorder.record(
            databaseName = "app.db",
            sql = "CREATE TABLE logs (id INT)",
            durationMs = 1L,
        )
        DatabaseLogRecorder.record(
            databaseName = "app.db",
            sql = "DROP TABLE old_logs",
            durationMs = 3L,
        )
        DatabaseLogRecorder.record(
            databaseName = "app.db",
            sql = "SELECT * FROM missing",
            durationMs = 4L,
            isSuccess = false,
            errorMessage = "no such table: missing",
        )

        // ALL
        vm.setLogFilter(DatabaseLogFilter.ALL)
        assertEquals(5, vm.filteredLogs.value.size)

        // SELECTS
        vm.setLogFilter(DatabaseLogFilter.SELECTS)
        assertEquals(1, vm.filteredLogs.value.size)
        assertEquals(
            "SELECT",
            vm.filteredLogs.value
                .first()
                .operation,
        )

        // INSERTS
        vm.setLogFilter(DatabaseLogFilter.INSERTS)
        assertEquals(1, vm.filteredLogs.value.size)

        // SCHEMA (includes CREATE, DROP)
        vm.setLogFilter(DatabaseLogFilter.SCHEMA)
        assertEquals(2, vm.filteredLogs.value.size)

        // ERRORS
        vm.setLogFilter(DatabaseLogFilter.ERRORS)
        assertEquals(1, vm.filteredLogs.value.size)
        assertFalse(
            vm.filteredLogs.value
                .first()
                .isSuccess,
        )

        // Search query
        vm.setLogFilter(DatabaseLogFilter.ALL)
        vm.setLogSearchQuery("logs")
        assertEquals(2, vm.filteredLogs.value.size)

        // Clear logs via VM
        vm.clearLogs()
        assertEquals(0, vm.filteredLogs.value.size)
    }

    @Test
    fun databaseProxy_convenienceLoggingMethods_recordProperOperations() {
        DatabaseProxy.logSelect(
            databaseName = "app.db",
            sql = "SELECT * FROM users WHERE active = ?",
            durationMs = 3L,
            rowCount = 15L,
            bindArgs = listOf(1),
        )
        DatabaseProxy.logInsert(
            databaseName = "app.db",
            sql = "INSERT INTO users (name, role) VALUES (?, ?)",
            durationMs = 5L,
            affectedRows = 1L,
            bindArgs = listOf("Alice", "Admin"),
        )
        DatabaseProxy.logUpdate(
            databaseName = "app.db",
            sql = "UPDATE users SET role = ? WHERE id = ?",
            durationMs = 4L,
            affectedRows = 2L,
            bindArgs = listOf("User", 10),
        )
        DatabaseProxy.logDelete(
            databaseName = "app.db",
            sql = "DELETE FROM users WHERE id = ?",
            durationMs = 2L,
            affectedRows = 1L,
            bindArgs = listOf(10),
        )
        DatabaseProxy.logError(
            databaseName = "app.db",
            sql = "SELECT * FROM invalid_table",
            error = IllegalStateException("Table not found"),
            durationMs = 1L,
        )

        val logs = DatabaseLogRecorder.logs.value
        assertEquals(5, logs.size)

        // Error is the latest (index 0)
        assertEquals("ERROR", logs[0].operation)
        assertFalse(logs[0].isSuccess)
        assertEquals("Table not found", logs[0].errorMessage)

        // Delete
        assertEquals("DELETE", logs[1].operation)
        assertEquals(1L, logs[1].affectedRows)
        assertTrue(logs[1].sql.contains("args: [10]"))

        // Update
        assertEquals("UPDATE", logs[2].operation)
        assertEquals(2L, logs[2].affectedRows)
        assertTrue(logs[2].sql.contains("args: [User, 10]"))

        // Insert
        assertEquals("INSERT", logs[3].operation)
        assertEquals(1L, logs[3].affectedRows)
        assertTrue(logs[3].sql.contains("args: [Alice, Admin]"))

        // Select
        assertEquals("SELECT", logs[4].operation)
        assertEquals(15L, logs[4].affectedRows)
        assertTrue(logs[4].sql.contains("args: [1]"))
    }

    @Test
    fun databaseProxy_trace_measuresDurationAndCapturesErrors() {
        // Trace successful block returning collection
        val resultList =
            DatabaseProxy.trace(
                databaseName = "app.db",
                sql = "SELECT * FROM products",
            ) {
                listOf("Item1", "Item2", "Item3")
            }
        assertEquals(3, resultList.size)

        val logs = DatabaseLogRecorder.logs.value
        assertEquals(1, logs.size)
        assertEquals("SELECT", logs.first().operation)
        assertTrue(logs.first().isSuccess)
        assertEquals(3L, logs.first().affectedRows)

        // Trace failing block
        var thrown = false
        try {
            DatabaseProxy.trace(
                databaseName = "app.db",
                sql = "UPDATE products SET price = -1",
            ) {
                throw IllegalArgumentException("Price cannot be negative")
            }
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue(thrown)

        val updatedLogs = DatabaseLogRecorder.logs.value
        assertEquals(2, updatedLogs.size)
        val errorLog = updatedLogs.first()
        assertEquals("ERROR", errorLog.operation)
        assertFalse(errorLog.isSuccess)
        assertEquals("Price cannot be negative", errorLog.errorMessage)
    }

    @Test
    fun databaseProxy_createQueryInterceptor_forwardsQueries() {
        val interceptor = DatabaseProxy.createQueryInterceptor("room.db")
        interceptor.onQuery("SELECT * FROM settings WHERE key = ?", listOf("theme"))

        val logs = DatabaseLogRecorder.logs.value
        assertEquals(1, logs.size)
        assertEquals("room.db", logs.first().databaseName)
        assertTrue(logs.first().sql.contains("theme"))
    }

    @Test
    fun databaseProxy_sqlDelightLogger_forwardsQueries() {
        val logger = DatabaseProxy.sqlDelightLogger("sqldelight.db")
        logger("SELECT * FROM items WHERE category = 'books'")

        val logs = DatabaseLogRecorder.logs.value
        assertEquals(1, logs.size)
        assertEquals("sqldelight.db", logs.first().databaseName)
        assertEquals("SELECT", logs.first().operation)
        assertEquals("items", logs.first().tableName)
    }
}
