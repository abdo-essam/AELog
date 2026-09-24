package com.ae.log.sample

import android.content.ContentValues
import android.content.Context

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

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT NOT NULL,
                role TEXT NOT NULL
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS products (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                price REAL NOT NULL,
                stock INTEGER NOT NULL,
                category TEXT NOT NULL
            );
            """.trimIndent(),
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS orders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                total REAL NOT NULL,
                status TEXT NOT NULL
            );
            """.trimIndent(),
        )

        // Seed users if empty
        val userCount =
            db.rawQuery("SELECT COUNT(*) FROM users", null).use { cursor ->
                if (cursor.moveToFirst()) cursor.getInt(0) else 0
            }

        if (userCount == 0) {
            db.execSQL("INSERT INTO users (name, email, role) VALUES ('Alice Smith', 'alice@example.com', 'Admin');")
            db.execSQL("INSERT INTO users (name, email, role) VALUES ('Bob Jones', 'bob@example.com', 'Developer');")
            db.execSQL(
                "INSERT INTO users (name, email, role) VALUES ('Charlie Brown', 'charlie@example.com', 'Designer');",
            )
            db.execSQL("INSERT INTO users (name, email, role) VALUES ('Diana Prince', 'diana@example.com', 'Manager');")

            db.execSQL(
                "INSERT INTO products (title, price, stock, category) VALUES ('MacBook Pro 16\"', 2499.00, 15, 'Hardware');",
            )
            db.execSQL(
                "INSERT INTO products (title, price, stock, category) VALUES ('Ergonomic Mouse', 59.99, 45, 'Accessories');",
            )
            db.execSQL(
                "INSERT INTO products (title, price, stock, category) VALUES ('Mechanical Keyboard', 129.50, 20, 'Accessories');",
            )
            db.execSQL(
                "INSERT INTO products (title, price, stock, category) VALUES ('4K UltraSharp Display', 599.00, 8, 'Monitors');",
            )

            db.execSQL("INSERT INTO orders (user_id, total, status) VALUES (1, 2558.99, 'Delivered');")
            db.execSQL("INSERT INTO orders (user_id, total, status) VALUES (2, 129.50, 'Shipped');")
            db.execSQL("INSERT INTO orders (user_id, total, status) VALUES (3, 59.99, 'Processing');")
        }

        db.close()
        "Ready: shop_sample.db created with users, products, and orders."
    } catch (e: Exception) {
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
        id != -1L
    } catch (_: Exception) {
        false
    }
}

public actual fun deleteSampleDatabase(): Boolean {
    val ctx = SampleAppContext.context ?: return false
    return try {
        ctx.deleteDatabase("shop_sample.db")
    } catch (_: Exception) {
        false
    }
}
