package com.ae.log.sample

import com.ae.log.database.inspector.DatabaseInspector
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult

private const val SAMPLE_DB_NAME = "ios_sample.db"
private const val SHOP_SAMPLE_DB_NAME = "shop_sample.db"
private const val TYPE_INTEGER = "INTEGER"
private const val TYPE_TEXT = "TEXT"
private const val TYPE_REAL = "REAL"
private const val COL_ID = "id"
private const val COL_NAME = "name"
private const val COL_EMAIL = "email"
private const val COL_ROLE = "role"
private const val COL_TITLE = "title"
private const val COL_PRICE = "price"
private const val COL_STOCK = "stock"
private const val COL_CATEGORY = "category"
private const val COL_USER_ID = "user_id"
private const val COL_TOTAL = "total"
private const val COL_STATUS = "status"

private val TABLE_INFO_COLUMNS = listOf("cid", COL_NAME, "type", "notnull", "dflt_value", "pk")
private val USERS_COLUMNS = listOf(COL_ID, COL_NAME, COL_EMAIL, COL_ROLE)
private val PRODUCTS_COLUMNS = listOf(COL_ID, COL_TITLE, COL_PRICE, COL_STOCK, COL_CATEGORY)
private val ORDERS_COLUMNS = listOf(COL_ID, COL_USER_ID, COL_TOTAL, COL_STATUS)

internal class SampleIosDatabaseInspector(
    private val delegate: DatabaseInspector,
) : DatabaseInspector {
    override fun registerDatabase(dbInfo: DbInfo) {
        delegate.registerDatabase(dbInfo)
    }

    override fun listDatabases(): List<DbInfo> {
        val list = delegate.listDatabases().toMutableList()
        if (list.none { it.name == SHOP_SAMPLE_DB_NAME }) {
            list.add(
                DbInfo(
                    name = SHOP_SAMPLE_DB_NAME,
                    path = SHOP_SAMPLE_DB_NAME,
                    engine = "SQLite",
                    tableCount = 5,
                    sizeBytes = 204800L,
                ),
            )
        }
        return list
    }

    override fun listTables(dbInfo: DbInfo): List<DbTable> {
        if (dbInfo.name == SAMPLE_DB_NAME || dbInfo.name == SHOP_SAMPLE_DB_NAME) {
            return listOf(
                DbTable(
                    name = "categories",
                    rowCount = 5L,
                    columns = listOf("id", "name", "slug"),
                    isSystemTable = false,
                ),
                DbTable(
                    name = "users",
                    rowCount = 120L,
                    columns = listOf("id", "name", "email", "role"),
                    isSystemTable = false,
                ),
                DbTable(
                    name = "products",
                    rowCount = 100L,
                    columns = listOf("id", "title", "price", "stock", "category_id"),
                    isSystemTable = false,
                ),
                DbTable(
                    name = "orders",
                    rowCount = 150L,
                    columns = listOf("id", "user_id", "total", "status"),
                    isSystemTable = false,
                ),
                DbTable(
                    name = "order_items",
                    rowCount = 200L,
                    columns = listOf("id", "order_id", "product_id", "quantity", "unit_price"),
                    isSystemTable = false,
                ),
            )
        }
        return delegate.listTables(dbInfo)
    }

    override fun query(
        dbInfo: DbInfo,
        sql: String,
        args: List<String>,
        allowWrite: Boolean,
    ): QueryResult {
        if (dbInfo.name == SAMPLE_DB_NAME || dbInfo.name == SHOP_SAMPLE_DB_NAME) {
            val clean = sql.trim().uppercase()
            return when {
                clean.contains("TABLE_INFO") && clean.contains("USERS") ->
                    QueryResult.success(
                        columns = TABLE_INFO_COLUMNS,
                        rows =
                            listOf(
                                listOf("0", COL_ID, TYPE_INTEGER, "1", null, "1"),
                                listOf("1", COL_NAME, TYPE_TEXT, "1", null, "0"),
                                listOf("2", COL_EMAIL, TYPE_TEXT, "1", null, "0"),
                                listOf("3", COL_ROLE, TYPE_TEXT, "1", null, "0"),
                            ),
                        durationMs = 1L,
                    )

                clean.contains("TABLE_INFO") && clean.contains("PRODUCTS") ->
                    QueryResult.success(
                        columns = TABLE_INFO_COLUMNS,
                        rows =
                            listOf(
                                listOf("0", COL_ID, TYPE_INTEGER, "1", null, "1"),
                                listOf("1", COL_TITLE, TYPE_TEXT, "1", null, "0"),
                                listOf("2", COL_PRICE, TYPE_REAL, "1", null, "0"),
                                listOf("3", COL_STOCK, TYPE_INTEGER, "1", null, "0"),
                                listOf("4", COL_CATEGORY, TYPE_TEXT, "1", null, "0"),
                            ),
                        durationMs = 1L,
                    )

                clean.contains("TABLE_INFO") && (clean.contains("ORDER_ITEMS") || clean.contains("ORDER_ITEM")) ->
                    QueryResult.success(
                        columns = TABLE_INFO_COLUMNS,
                        rows =
                            listOf(
                                listOf("0", "id", TYPE_INTEGER, "1", null, "1"),
                                listOf("1", "order_id", TYPE_INTEGER, "1", null, "0"),
                                listOf("2", "product_id", TYPE_INTEGER, "1", null, "0"),
                                listOf("3", "quantity", TYPE_INTEGER, "1", null, "0"),
                                listOf("4", "unit_price", TYPE_REAL, "1", null, "0"),
                            ),
                        durationMs = 1L,
                    )

                clean.contains("TABLE_INFO") && (clean.contains("CATEGORIES") || clean.contains("CATEGORY")) ->
                    QueryResult.success(
                        columns = TABLE_INFO_COLUMNS,
                        rows =
                            listOf(
                                listOf("0", "id", TYPE_INTEGER, "1", null, "1"),
                                listOf("1", "name", TYPE_TEXT, "1", null, "0"),
                                listOf("2", "slug", TYPE_TEXT, "1", null, "0"),
                            ),
                        durationMs = 1L,
                    )

                clean.contains("TABLE_INFO") && clean.contains("ORDERS") ->
                    QueryResult.success(
                        columns = TABLE_INFO_COLUMNS,
                        rows =
                            listOf(
                                listOf("0", COL_ID, TYPE_INTEGER, "1", null, "1"),
                                listOf("1", COL_USER_ID, TYPE_INTEGER, "1", null, "0"),
                                listOf("2", COL_TOTAL, TYPE_REAL, "1", null, "0"),
                                listOf("3", COL_STATUS, TYPE_TEXT, "1", null, "0"),
                            ),
                        durationMs = 1L,
                    )

                clean.startsWith("PRAGMA INDEX_LIST") ->
                    QueryResult.success(
                        columns = listOf("seq", COL_NAME, "unique", "origin", "partial"),
                        rows = emptyList(),
                        durationMs = 1L,
                    )

                clean.contains("FROM \"USERS\"") || clean.contains("FROM USERS") ->
                    buildUsersQueryResult(sql)

                clean.contains("FROM \"PRODUCTS\"") || clean.contains("FROM PRODUCTS") ->
                    buildProductsQueryResult(sql)

                clean.contains("FROM \"ORDER_ITEMS\"") || clean.contains("FROM ORDER_ITEMS") ->
                    buildOrderItemsQueryResult(sql)

                clean.contains("FROM \"CATEGORIES\"") || clean.contains("FROM CATEGORIES") ->
                    QueryResult.success(
                        columns = listOf("id", "name", "slug"),
                        rows =
                            listOf(
                                listOf("1", "Hardware", "hardware"),
                                listOf("2", "Accessories", "accessories"),
                                listOf("3", "Monitors", "monitors"),
                                listOf("4", "Software", "software"),
                                listOf("5", "Peripherals", "peripherals"),
                            ),
                        durationMs = 2L,
                    )

                clean.contains("FROM \"ORDERS\"") || clean.contains("FROM ORDERS") ->
                    buildOrdersQueryResult(sql)

                else ->
                    QueryResult.success(
                        columns = listOf("result"),
                        rows = listOf(listOf("OK")),
                        durationMs = 1L,
                    )
            }
        }
        return delegate.query(dbInfo, sql, args, allowWrite)
    }

    private fun buildUsersQueryResult(sql: String): QueryResult {
        val limit = parseSqlLimit(sql)
        val offset = parseSqlOffset(sql)
        val totalItems = 120
        val firstNames =
            listOf(
                "Alice",
                "Bob",
                "Charlie",
                "Diana",
                "Ethan",
                "Fiona",
                "George",
                "Hannah",
                "Ian",
                "Julia",
            )
        val lastNames =
            listOf(
                "Smith",
                "Jones",
                "Brown",
                "Prince",
                "Miller",
                "Davis",
                "Wilson",
                "Taylor",
                "Anderson",
                "Thomas",
            )
        val roles = listOf("Admin", "Developer", "Designer", "Manager", "User")
        val allItems =
            (1..totalItems).map { id ->
                val firstName = firstNames[(id - 1) % firstNames.size]
                val lastName = lastNames[(id - 1) % lastNames.size]
                val name = "$firstName $lastName"
                val email = "${firstName.lowercase()}.$id@example.com"
                val role = roles[(id - 1) % roles.size]
                listOf(id.toString(), name, email, role)
            }
        val pagedRows = allItems.drop(offset).take(limit)
        return QueryResult.success(
            columns = USERS_COLUMNS,
            rows = pagedRows,
            durationMs = 2L,
        )
    }

    private fun buildProductsQueryResult(sql: String): QueryResult {
        val limit = parseSqlLimit(sql)
        val offset = parseSqlOffset(sql)
        val totalItems = 100
        val allItems =
            (1..totalItems).map { id ->
                val title =
                    when (id % 5) {
                        1 -> "MacBook Pro 16\" #$id"
                        2 -> "Ergonomic Mouse #$id"
                        3 -> "Mechanical Keyboard #$id"
                        4 -> "4K UltraSharp Display #$id"
                        else -> "USB-C Hub #$id"
                    }
                val price = (19.99 + (id * 12.5)).toString()
                val stock = ((id * 7) % 80 + 5).toString()
                val categoryId = ((id % 4) + 1).toString()
                listOf(id.toString(), title, price, stock, categoryId)
            }
        val pagedRows = allItems.drop(offset).take(limit)
        return QueryResult.success(
            columns = PRODUCTS_COLUMNS,
            rows = pagedRows,
            durationMs = 2L,
        )
    }

    private fun buildOrdersQueryResult(sql: String): QueryResult {
        val limit = parseSqlLimit(sql)
        val offset = parseSqlOffset(sql)
        val totalItems = 150
        val statuses = listOf("Delivered", "Shipped", "Processing", "Cancelled", "Pending")
        val allItems =
            (1..totalItems).map { id ->
                val userId = ((id - 1) % 120) + 1
                val total = (49.99 + (id * 18.25)).toString()
                val status = statuses[(id - 1) % statuses.size]
                listOf(id.toString(), userId.toString(), total, status)
            }
        val pagedRows = allItems.drop(offset).take(limit)
        return QueryResult.success(
            columns = ORDERS_COLUMNS,
            rows = pagedRows,
            durationMs = 2L,
        )
    }

    private fun buildOrderItemsQueryResult(sql: String): QueryResult {
        val limit = parseSqlLimit(sql)
        val offset = parseSqlOffset(sql)
        val totalItems = 200
        val allItems =
            (1..totalItems).map { id ->
                val orderId = ((id - 1) % 150) + 1
                val productId = ((id - 1) % 100) + 1
                val qty = ((id - 1) % 5) + 1
                val price =
                    when (productId % 4) {
                        0 -> "2499.00"
                        1 -> "45.00"
                        2 -> "120.00"
                        else -> "599.00"
                    }
                listOf(id.toString(), orderId.toString(), productId.toString(), qty.toString(), price)
            }
        val pagedRows = allItems.drop(offset).take(limit)
        return QueryResult.success(
            columns = listOf("id", "order_id", "product_id", "quantity", "unit_price"),
            rows = pagedRows,
            durationMs = 2L,
        )
    }

    private fun parseSqlLimit(sql: String): Int {
        val uppercase = sql.uppercase()
        val limitIdx = uppercase.indexOf("LIMIT ")
        if (limitIdx != -1) {
            val substring = sql.substring(limitIdx + 6).trim()
            val spaceIdx = substring.indexOf(' ')
            val numStr = if (spaceIdx != -1) substring.substring(0, spaceIdx) else substring
            return numStr.toIntOrNull() ?: 50
        }
        return 50
    }

    private fun parseSqlOffset(sql: String): Int {
        val uppercase = sql.uppercase()
        val offsetIdx = uppercase.indexOf("OFFSET ")
        if (offsetIdx != -1) {
            val substring = sql.substring(offsetIdx + 7).trim()
            val spaceIdx = substring.indexOf(' ')
            val numStr = if (spaceIdx != -1) substring.substring(0, spaceIdx) else substring
            return numStr.toIntOrNull() ?: 0
        }
        return 0
    }
}
