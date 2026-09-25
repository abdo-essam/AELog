package com.ae.log.sample

import com.ae.log.AELog
import com.ae.log.database.DatabasePlugin
import com.ae.log.database.database
import com.ae.log.database.model.DbInfo

private const val DB_NAME = "shop_sample.db"

public actual fun ensureSampleDatabaseExists(): String {
    val dbInfo =
        DbInfo(
            name = DB_NAME,
            path = DB_NAME,
            engine = "SQLite",
            tableCount = 5,
            sizeBytes = 204800L,
        )

    val dbPlugin = AELog.getPlugin<DatabasePlugin>()
    if (dbPlugin != null) {
        if (dbPlugin.inspector !is SampleIosDatabaseInspector) {
            dbPlugin.inspector = SampleIosDatabaseInspector(dbPlugin.inspector)
        }
        dbPlugin.inspector.registerDatabase(dbInfo)
    } else {
        AELog.database.registerDatabase(dbInfo)
    }

    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "CREATE TABLE IF NOT EXISTS categories (id INTEGER PRIMARY KEY, name TEXT, slug TEXT UNIQUE);",
        durationMs = 2L,
        tableName = "categories",
    )
    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT, email TEXT, role TEXT);",
        durationMs = 2L,
        tableName = "users",
    )
    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY, title TEXT, price REAL, stock INT, category_id INT, FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL);",
        durationMs = 2L,
        tableName = "products",
    )
    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "CREATE TABLE IF NOT EXISTS orders (id INTEGER PRIMARY KEY, user_id INT, total REAL, status TEXT, FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE);",
        durationMs = 2L,
        tableName = "orders",
    )
    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "CREATE TABLE IF NOT EXISTS order_items (id INTEGER PRIMARY KEY, order_id INT, product_id INT, quantity INT, unit_price REAL, FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE, FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE RESTRICT);",
        durationMs = 2L,
        tableName = "order_items",
    )
    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);",
        durationMs = 1L,
        tableName = "orders",
    )

    // Example Inserts
    AELog.database.logInsert(
        databaseName = DB_NAME,
        sql = "INSERT INTO categories (name, slug) VALUES ('Hardware', 'hardware');",
        durationMs = 2L,
        tableName = "categories",
    )
    AELog.database.logInsert(
        databaseName = DB_NAME,
        sql = "INSERT INTO users (name, email, role) VALUES ('Alice Smith', 'alice@example.com', 'Admin');",
        durationMs = 3L,
        tableName = "users",
    )
    AELog.database.logInsert(
        databaseName = DB_NAME,
        sql = "INSERT INTO products (title, price, stock, category_id) VALUES ('MacBook Pro 16\"', 2499.00, 15, 1);",
        durationMs = 3L,
        tableName = "products",
    )
    AELog.database.logInsert(
        databaseName = DB_NAME,
        sql = "INSERT INTO orders (user_id, total, status) VALUES (1, 2558.99, 'Delivered');",
        durationMs = 3L,
        tableName = "orders",
    )

    // Example Selects
    AELog.database.logSelect(
        databaseName = DB_NAME,
        sql = "SELECT * FROM products WHERE stock > 10;",
        durationMs = 2L,
        tableName = "products",
        rowCount = 3L,
    )
    AELog.database.logSelect(
        databaseName = DB_NAME,
        sql = "SELECT * FROM orders WHERE status = 'Shipped';",
        durationMs = 1L,
        tableName = "orders",
        rowCount = 1L,
    )

    // Example Update
    AELog.database.logUpdate(
        databaseName = DB_NAME,
        sql = "UPDATE products SET stock = stock - 1 WHERE id = 1;",
        durationMs = 4L,
        tableName = "products",
        affectedRows = 1L,
    )

    // Example Delete
    AELog.database.logDelete(
        databaseName = DB_NAME,
        sql = "DELETE FROM order_items WHERE id = 999;",
        durationMs = 2L,
        tableName = "order_items",
        affectedRows = 0L,
    )

    // Example Error
    AELog.database.logError(
        databaseName = DB_NAME,
        sql = "SELECT * FROM non_existing_table;",
        error = IllegalStateException("no such table: non_existing_table"),
        durationMs = 1L,
    )

    return "Sample database initialized for iOS"
}

public actual fun insertSampleUser(
    name: String,
    email: String,
    role: String,
): Boolean {
    AELog.database.logInsert(
        databaseName = DB_NAME,
        sql = "INSERT INTO users (name, email, role) VALUES ('$name', '$email', '$role');",
        durationMs = 3L,
        tableName = "users",
    )
    return true
}

public actual fun deleteSampleDatabase(): Boolean {
    AELog.database.logQuery(
        databaseName = DB_NAME,
        sql = "DROP DATABASE $DB_NAME;",
        durationMs = 5L,
        isSuccess = true,
    )
    return true
}
