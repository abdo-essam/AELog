package com.ae.log.database

import com.ae.log.database.inspector.isWriteStatement
import com.ae.log.database.inspector.validateSqlSafety
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SqlSecurityTest {
    @Test
    fun isWriteStatement_identifiesWriteKeywordsCorrectly() {
        assertTrue(isWriteStatement("INSERT INTO users VALUES (1, 'John')"))
        assertTrue(isWriteStatement("UPDATE users SET name = 'Jane'"))
        assertTrue(isWriteStatement("DELETE FROM users WHERE id = 1"))
        assertTrue(isWriteStatement("DROP TABLE users"))
        assertTrue(isWriteStatement("CREATE TABLE test (id INT)"))
        assertTrue(isWriteStatement("ALTER TABLE test ADD COLUMN age INT"))
        assertTrue(isWriteStatement("REPLACE INTO users VALUES (2, 'Bob')"))
        assertTrue(isWriteStatement("TRUNCATE TABLE users"))
        assertTrue(isWriteStatement("VACUUM"))
        assertTrue(isWriteStatement("   insert into lowercase values (1)"))
    }

    @Test
    fun isWriteStatement_identifiesReadStatementsCorrectly() {
        assertFalse(isWriteStatement("SELECT * FROM users"))
        assertFalse(isWriteStatement("SELECT COUNT(*) FROM users WHERE active = 1"))
        assertFalse(isWriteStatement("PRAGMA table_info('users')"))
        assertFalse(isWriteStatement("PRAGMA user_version"))
        assertFalse(isWriteStatement("   select id from users"))
    }

    @Test
    fun validateSqlSafety_blocksWriteWhenAllowWriteIsFalse() {
        assertFailsWith<IllegalArgumentException> {
            validateSqlSafety("DELETE FROM users", allowWrite = false)
        }
        assertFailsWith<IllegalArgumentException> {
            validateSqlSafety("DROP TABLE users", allowWrite = false)
        }
    }

    @Test
    fun validateSqlSafety_allowsWriteWhenAllowWriteIsTrue() {
        validateSqlSafety("DELETE FROM users", allowWrite = true)
        validateSqlSafety("UPDATE users SET active = 0", allowWrite = true)
    }

    @Test
    fun validateSqlSafety_alwaysAllowsSelectAndPragma() {
        validateSqlSafety("SELECT * FROM users", allowWrite = false)
        validateSqlSafety("PRAGMA table_info(users)", allowWrite = false)
    }

    @Test
    fun getTableData_protectsAgainstSqlInjectionInSearchAndSort() {
        var executedSql = ""
        val inspector =
            object : com.ae.log.database.inspector.BaseDatabaseInspector() {
                override fun listDatabases(): List<com.ae.log.database.model.DbInfo> =
                    listOf(
                        com.ae.log.database.model
                            .DbInfo("test.db", "/test.db"),
                    )

                override fun listTables(
                    dbInfo: com.ae.log.database.model.DbInfo,
                ): List<com.ae.log.database.model.DbTable> =
                    listOf(
                        com.ae.log.database.model
                            .DbTable("users"),
                    )

                override fun query(
                    dbInfo: com.ae.log.database.model.DbInfo,
                    sql: String,
                    args: List<String>,
                    allowWrite: Boolean,
                    recordLog: Boolean,
                ): com.ae.log.database.model.QueryResult {
                    executedSql = sql
                    if (sql.contains("PRAGMA table_info")) {
                        return com.ae.log.database.model.QueryResult.success(
                            columns = listOf("cid", "name", "type", "notnull", "dflt_value", "pk"),
                            rows =
                                listOf(
                                    listOf("0", "id", "INTEGER", "1", null, "1"),
                                    listOf("1", "username", "TEXT", "1", null, "0"),
                                ),
                        )
                    }
                    return com.ae.log.database.model.QueryResult
                        .success(listOf("id", "username"), emptyList())
                }
            }

        val db =
            com.ae.log.database.model
                .DbInfo("test.db", "/test.db")

        // 1. Injected sort column not in schema must be omitted
        inspector.getTableData(
            dbInfo = db,
            tableName = "users",
            sortColumn = "id; DROP TABLE users; --",
        )
        assertFalse(executedSql.contains("DROP TABLE"))
        assertFalse(executedSql.contains("ORDER BY"))

        // 2. Legitimate sort column should be included with quotes
        inspector.getTableData(
            dbInfo = db,
            tableName = "users",
            sortColumn = "username",
            sortAscending = false,
        )
        assertTrue(executedSql.contains("ORDER BY \"username\" DESC"))

        // 3. Injected search query must be escaped
        inspector.getTableData(
            dbInfo = db,
            tableName = "users",
            searchQuery = "admin' OR '1'='1",
        )
        assertTrue(executedSql.contains("admin'' OR ''1''=''1"))
        assertTrue(executedSql.contains("ESCAPE '\\'"))
        assertFalse(executedSql.contains("admin' OR '1'='1"))
    }
}
