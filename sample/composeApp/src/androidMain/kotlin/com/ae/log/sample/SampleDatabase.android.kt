package com.ae.log.sample

import android.content.ContentValues
import android.content.Context
import com.ae.log.AELog
import com.ae.log.database.database

internal object SampleAppContext {
    @Volatile
    var context: Context? = null

    fun init(ctx: Context) {
        if (context == null) {
            context = ctx.applicationContext
        }
    }
}

public actual fun ensureSampleDatabaseExists(): String {
    val ctx = SampleAppContext.context ?: return "Error: Android Context not initialized"

    return try {
        val db = ctx.openOrCreateDatabase("shop_sample.db", Context.MODE_PRIVATE, null)

        val createUsersSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(createUsersSql)
        AELog.database.logQuery("shop_sample.db", createUsersSql, durationMs = 2L, tableName = "users")

        val createProductsSql = """
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                category TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(createProductsSql)
        AELog.database.logQuery("shop_sample.db", createProductsSql, durationMs = 2L, tableName = "products")

        val createOrdersSql = """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                total REAL NOT NULL,
                status TEXT NOT NULL
            );
        """.trimIndent()
        db.execSQL(createOrdersSql)
        AELog.database.logQuery("shop_sample.db", createOrdersSql, durationMs = 2L, tableName = "orders")

        // Seed users if empty
        val userCount =
            db.rawQuery("SELECT COUNT(*) FROM users", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }
        AELog.database.logQuery("shop_sample.db", "SELECT COUNT(*) FROM users", durationMs = 1L, tableName = "users")

        if (userCount == 0) {
            val userSeeds = listOf(
                "INSERT INTO users (name, email, role) VALUES ('Alice Smith', 'alice@example.com', 'Admin');",
                "INSERT INTO users (name, email, role) VALUES ('Bob Jones', 'bob@example.com', 'Developer');",
                "INSERT INTO users (name, email, role) VALUES ('Charlie Brown', 'charlie@example.com', 'Designer');",
                "INSERT INTO users (name, email, role) VALUES ('Diana Prince', 'diana@example.com', 'Manager');",
            )
            userSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logQuery("shop_sample.db", sql, durationMs = 3L, tableName = "users", affectedRows = 1L)
            }

            val productSeeds = listOf(
                "INSERT INTO products (title, price, stock, category) VALUES ('MacBook Pro 16\"', 2499.00, 15, 'Hardware');",
                "INSERT INTO products (title, price, stock, category) VALUES ('Ergonomic Mouse', 59.99, 45, 'Accessories');",
                "INSERT INTO products (title, price, stock, category) VALUES ('Mechanical Keyboard', 129.50, 20, 'Accessories');",
                "INSERT INTO products (title, price, stock, category) VALUES ('4K UltraSharp Display', 599.00, 8, 'Monitors');",
            )
            productSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logQuery("shop_sample.db", sql, durationMs = 3L, tableName = "products", affectedRows = 1L)
            }

            val orderSeeds = listOf(
                "INSERT INTO orders (user_id, total, status) VALUES (1, 2558.99, 'Delivered');",
                "INSERT INTO orders (user_id, total, status) VALUES (2, 129.50, 'Shipped');",
                "INSERT INTO orders (user_id, total, status) VALUES (3, 59.99, 'Processing');",
            )
            orderSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logQuery("shop_sample.db", sql, durationMs = 3L, tableName = "orders", affectedRows = 1L)
            }
        }

        db.close()
        "Ready: shop_sample.db created with users, products, and orders."
    } catch (e: Exception) {
        AELog.database.logQuery(
            databaseName = "shop_sample.db",
            sql = "INIT DATABASE shop_sample.db",
            durationMs = 0L,
            isSuccess = false,
            errorMessage = e.message,
        )
        "Failed to create database: ${e.message}"
    }
}

public actual fun insertSampleUser(
    name: String,
    email: String,
    role: String,
): Boolean {
    val ctx = SampleAppContext.context ?: return false
    return try {
        val db = ctx.openOrCreateDatabase("shop_sample.db", Context.MODE_PRIVATE, null)
        val values =
            ContentValues().apply {
                put("name", name)
                put("email", email)
                put("role", role)
            }
        val id = db.insert("users", null, values)
        db.close()
        val success = id != -1L
        AELog.database.logQuery(
            databaseName = "shop_sample.db",
            sql = "INSERT INTO users (name, email, role) VALUES ('$name', '$email', '$role');",
            durationMs = 4L,
            tableName = "users",
            isSuccess = success,
            affectedRows = if (success) 1L else 0L,
        )
        success
    } catch (e: Exception) {
        AELog.database.logQuery(
            databaseName = "shop_sample.db",
            sql = "INSERT INTO users (name, email, role) VALUES ('$name', '$email', '$role');",
            durationMs = 0L,
            tableName = "users",
            isSuccess = false,
            errorMessage = e.message,
        )
        false
    }
}

public actual fun deleteSampleDatabase(): Boolean {
    val ctx = SampleAppContext.context ?: return false
    return try {
        val deleted = ctx.deleteDatabase("shop_sample.db")
        AELog.database.logQuery(
            databaseName = "shop_sample.db",
            sql = "DROP DATABASE shop_sample.db;",
            durationMs = 8L,
            isSuccess = deleted,
        )
        deleted
    } catch (e: Exception) {
        AELog.database.logQuery(
            databaseName = "shop_sample.db",
            sql = "DROP DATABASE shop_sample.db;",
            durationMs = 0L,
            isSuccess = false,
            errorMessage = e.message,
        )
        false
    }
}
