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
}
