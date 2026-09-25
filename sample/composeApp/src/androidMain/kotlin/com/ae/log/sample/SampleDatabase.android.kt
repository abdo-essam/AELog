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
        db.execSQL("PRAGMA foreign_keys = ON;")

        val createCategoriesSql =
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                slug TEXT UNIQUE NOT NULL
            );
            """.trimIndent()
        db.execSQL(createCategoriesSql)
        AELog.database.logQuery("shop_sample.db", createCategoriesSql, durationMs = 2L, tableName = "categories")

        val createUsersSql =
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL
            );
            """.trimIndent()
        db.execSQL(createUsersSql)
        AELog.database.logQuery("shop_sample.db", createUsersSql, durationMs = 2L, tableName = "users")

        val createProductsSql =
            """
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                category_id INTEGER,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
            );
            """.trimIndent()
        db.execSQL(createProductsSql)
        AELog.database.logQuery("shop_sample.db", createProductsSql, durationMs = 2L, tableName = "products")

        val createOrdersSql =
            """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                total REAL NOT NULL,
                status TEXT NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            );
            """.trimIndent()
        db.execSQL(createOrdersSql)
        AELog.database.logQuery("shop_sample.db", createOrdersSql, durationMs = 2L, tableName = "orders")

        val createOrderItemsSql =
            """
            CREATE TABLE IF NOT EXISTS order_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                order_id INTEGER NOT NULL,
                product_id INTEGER NOT NULL,
                quantity INTEGER NOT NULL,
                unit_price REAL NOT NULL,
                FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT
            );
            """.trimIndent()
        db.execSQL(createOrderItemsSql)
        AELog.database.logQuery("shop_sample.db", createOrderItemsSql, durationMs = 2L, tableName = "order_items")

        val createIndexSql = "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);"
        db.execSQL(createIndexSql)
        AELog.database.logQuery("shop_sample.db", createIndexSql, durationMs = 1L, tableName = "orders")

        // Seed if empty
        val userCount =
            db.rawQuery("SELECT COUNT(*) FROM users", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }

        if (userCount == 0) {
            val categorySeeds =
                listOf(
                    "INSERT INTO categories (name, slug) VALUES ('Hardware', 'hardware');",
                    "INSERT INTO categories (name, slug) VALUES ('Accessories', 'accessories');",
                    "INSERT INTO categories (name, slug) VALUES ('Monitors', 'monitors');",
                )
            categorySeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logInsert("shop_sample.db", sql, durationMs = 2L, tableName = "categories")
            }

            val userSeeds =
                listOf(
                    "INSERT INTO users (name, email, role) VALUES ('Alice Smith', 'alice@example.com', 'Admin');",
                    "INSERT INTO users (name, email, role) VALUES ('Bob Jones', 'bob@example.com', 'Developer');",
                    "INSERT INTO users (name, email, role) VALUES ('Charlie Brown', 'charlie@example.com', 'Designer');",
                    "INSERT INTO users (name, email, role) VALUES ('Diana Prince', 'diana@example.com', 'Manager');",
                )
            userSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logInsert("shop_sample.db", sql, durationMs = 3L, tableName = "users")
            }

            val productSeeds =
                listOf(
                    "INSERT INTO products (title, price, stock, category_id) VALUES ('MacBook Pro 16\"', 2499.00, 15, 1);",
                    "INSERT INTO products (title, price, stock, category_id) VALUES ('Ergonomic Mouse', 59.99, 45, 2);",
                    "INSERT INTO products (title, price, stock, category_id) VALUES ('Mechanical Keyboard', 129.50, 20, 2);",
                    "INSERT INTO products (title, price, stock, category_id) VALUES ('4K UltraSharp Display', 599.00, 8, 3);",
                )
            productSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logInsert("shop_sample.db", sql, durationMs = 3L, tableName = "products")
            }

            val orderSeeds =
                listOf(
                    "INSERT INTO orders (user_id, total, status) VALUES (1, 2558.99, 'Delivered');",
                    "INSERT INTO orders (user_id, total, status) VALUES (2, 129.50, 'Shipped');",
                    "INSERT INTO orders (user_id, total, status) VALUES (3, 59.99, 'Processing');",
                )
            orderSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logInsert("shop_sample.db", sql, durationMs = 3L, tableName = "orders")
            }

            val orderItemSeeds =
                listOf(
                    "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1, 1, 1, 2499.00);",
                    "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (1, 2, 1, 59.99);",
                    "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (2, 3, 1, 129.50);",
                )
            orderItemSeeds.forEach { sql ->
                db.execSQL(sql)
                AELog.database.logInsert("shop_sample.db", sql, durationMs = 2L, tableName = "order_items")
            }

            AELog.database.logSelect(
                "shop_sample.db",
                "SELECT * FROM products WHERE stock > 10;",
                durationMs = 2L,
                tableName = "products",
                rowCount = 3L,
            )
            AELog.database.logSelect(
                "shop_sample.db",
                "SELECT * FROM orders WHERE status = 'Shipped';",
                durationMs = 1L,
                tableName = "orders",
                rowCount = 1L,
            )

            val updateSql = "UPDATE products SET stock = stock - 1 WHERE id = 1;"
            db.execSQL(updateSql)
            AELog.database.logUpdate("shop_sample.db", updateSql, durationMs = 4L, tableName = "products", affectedRows = 1L)

            AELog.database.logDelete("shop_sample.db", "DELETE FROM order_items WHERE id = 999;", durationMs = 2L, tableName = "order_items", affectedRows = 0L)

            AELog.database.logError(
                databaseName = "shop_sample.db",
                sql = "SELECT * FROM non_existing_table;",
                error = IllegalStateException("no such table: non_existing_table"),
                durationMs = 1L,
            )
        }

        db.close()
        "Ready: shop_sample.db created with categories, users, products, orders, order_items, and FK constraints."
    } catch (e: Exception) {
        AELog.database.logError(
            databaseName = "shop_sample.db",
            sql = "INIT DATABASE shop_sample.db",
            error = e,
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
