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
            db.beginTransaction()
            try {
                // Seed 5 Categories
                val categories = listOf("Hardware", "Accessories", "Monitors", "Audio", "Storage")
                categories.forEachIndexed { i, name ->
                    val slug = name.lowercase()
                    db.execSQL("INSERT INTO categories (name, slug) VALUES ('$name', '$slug');")
                }

                // Seed 120 Users
                val roles = listOf("Admin", "Developer", "Designer", "Manager", "Engineer", "User")
                val names = listOf("Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace", "Henry", "Ivy", "Jack")
                val surnames =
                    listOf("Smith", "Jones", "Brown", "Prince", "Adams", "Miller", "Davis", "Wilson", "Taylor", "Evans")
                for (i in 1..120) {
                    val fn = names[(i - 1) % names.size]
                    val sn = surnames[(i / names.size) % surnames.size]
                    val role = roles[i % roles.size]
                    val email = "${fn.lowercase()}.${sn.lowercase()}$i@example.com"
                    db.execSQL("INSERT INTO users (name, email, role) VALUES ('$fn $sn #$i', '$email', '$role');")
                }

                // Seed 100 Products
                val productTypes =
                    listOf(
                        "Laptop",
                        "Mouse",
                        "Keyboard",
                        "Display",
                        "Headphones",
                        "SSD Drive",
                        "USB Hub",
                        "Webcam",
                        "Speaker",
                        "Monitor Arm",
                    )
                for (i in 1..100) {
                    val pName = "${productTypes[i % productTypes.size]} Pro #$i"
                    val price = 29.99 + (i * 18.5)
                    val stock = (i * 7) % 80
                    val catId = (i % 5) + 1
                    db.execSQL(
                        "INSERT INTO products (title, price, stock, category_id) VALUES ('$pName', $price, $stock, $catId);",
                    )
                }

                // Seed 150 Orders
                val statuses = listOf("Delivered", "Shipped", "Processing", "Pending", "Cancelled")
                for (i in 1..150) {
                    val userId = (i % 120) + 1
                    val total = 49.99 + (i * 24.5)
                    val status = statuses[i % statuses.size]
                    db.execSQL("INSERT INTO orders (user_id, total, status) VALUES ($userId, $total, '$status');")
                }

                // Seed 200 Order Items
                for (i in 1..200) {
                    val orderId = (i % 150) + 1
                    val productId = (i % 100) + 1
                    val qty = (i % 4) + 1
                    val unitPrice = 29.99 + (productId * 12.0)
                    db.execSQL(
                        "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES ($orderId, $productId, $qty, $unitPrice);",
                    )
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }

            // Log representative queries to showcase the Database Logs panel
            AELog.database.logInsert(
                "shop_sample.db",
                "INSERT INTO users (name, email, role) VALUES ('Alice Smith #121', 'alice121@example.com', 'Admin');",
                durationMs = 3L,
                tableName = "users",
            )
            AELog.database.logInsert(
                "shop_sample.db",
                "INSERT INTO products (title, price, stock, category_id) VALUES ('UltraWide Monitor 34\"', 899.00, 12, 3);",
                durationMs = 2L,
                tableName = "products",
            )
            AELog.database.logSelect(
                "shop_sample.db",
                "SELECT * FROM users WHERE role = 'Admin' ORDER BY id DESC LIMIT 50;",
                durationMs = 2L,
                tableName = "users",
                rowCount = 50L,
            )
            AELog.database.logSelect(
                "shop_sample.db",
                "SELECT * FROM products WHERE stock > 10 ORDER BY price DESC LIMIT 50;",
                durationMs = 3L,
                tableName = "products",
                rowCount = 50L,
            )
            AELog.database.logSelect(
                "shop_sample.db",
                "SELECT * FROM orders WHERE status = 'Delivered' LIMIT 50;",
                durationMs = 2L,
                tableName = "orders",
                rowCount = 50L,
            )
            AELog.database.logUpdate(
                "shop_sample.db",
                "UPDATE products SET stock = stock - 1 WHERE id = 1;",
                durationMs = 4L,
                tableName = "products",
                affectedRows = 1L,
            )
            AELog.database.logDelete(
                "shop_sample.db",
                "DELETE FROM order_items WHERE id = 999;",
                durationMs = 2L,
                tableName = "order_items",
                affectedRows = 0L,
            )
            AELog.database.logError(
                "shop_sample.db",
                "SELECT * FROM non_existing_table;",
                IllegalStateException("no such table: non_existing_table"),
                durationMs = 1L,
            )
        }

        db.close()
        "Ready: shop_sample.db created with 120 users, 100 products, 150 orders, 200 order_items, and FK constraints."
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
