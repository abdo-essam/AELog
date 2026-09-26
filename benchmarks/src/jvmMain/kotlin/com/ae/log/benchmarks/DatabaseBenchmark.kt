package com.ae.log.benchmarks

import com.ae.log.database.DatabaseLogRecorder
import com.ae.log.database.model.DatabaseLogEntry
import com.ae.log.database.ui.DatabaseFormatUtils
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.BenchmarkMode
import kotlinx.benchmark.BenchmarkTimeUnit
import kotlinx.benchmark.Mode
import kotlinx.benchmark.OutputTimeUnit
import kotlinx.benchmark.Param
import kotlinx.benchmark.Scope
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup

/**
 * JMH benchmarks for the AELog Database Plugin hot paths.
 *
 * Key operations benchmarked:
 * - [recordQueryLog]: Hot-path cost of capturing a single SQL query in real-time.
 * - [filterSelectLogs]: Filtering recorded logs by category (e.g. SELECTs).
 * - [searchLogs]: Full-text search across SQL statements, table names, and errors.
 * - [formatSingleLogForCopy]: Formatting a single database log entry into plain text.
 * - [formatAllLogsForCopy]: Formatting hundreds of recorded logs into clipboard text.
 * - [rowToJson] & [rowToSqlInsert]: Export utilities formatting database rows.
 *
 * Run:
 *   ./gradlew :benchmarks:jvmBenchmark --args ".*DatabaseBenchmark.*"
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(BenchmarkTimeUnit.MICROSECONDS)
@Warmup(iterations = 3)
open class DatabaseBenchmark {
    @Param("100", "500")
    var logCount: Int = 0

    private lateinit var sampleRow: Map<String, String?>
    private lateinit var sampleLogEntry: DatabaseLogEntry
    private lateinit var prefilledLogs: List<DatabaseLogEntry>

    @Setup
    fun setup() {
        DatabaseLogRecorder.clear()

        sampleRow =
            mapOf(
                "id" to "101",
                "name" to "Alice Smith",
                "email" to "alice@example.com",
                "role" to "Admin",
                "created_at" to "2026-09-25T14:50:00Z",
                "updated_at" to null,
            )

        sampleLogEntry =
            DatabaseLogEntry(
                id = "db_test_123",
                databaseName = "shop_sample.db",
                tableName = "users",
                sql = "SELECT id, name, email, role FROM users WHERE role = 'Admin' ORDER BY id DESC LIMIT 50;",
                operation = "SELECT",
                durationMs = 3L,
                timestamp = 1727280000000L,
                isSuccess = true,
                affectedRows = 50L,
                engine = "SQLite",
            )

        repeat(logCount) { i ->
            val op =
                when (i % 5) {
                    0 -> "SELECT"
                    1 -> "INSERT"
                    2 -> "UPDATE"
                    3 -> "DELETE"
                    else -> "ERROR"
                }
            val table =
                when (i % 4) {
                    0 -> "users"
                    1 -> "products"
                    2 -> "orders"
                    else -> "categories"
                }
            DatabaseLogRecorder.record(
                databaseName = "shop_sample.db",
                sql = "SELECT * FROM $table WHERE id = $i;",
                durationMs = (i % 10).toLong() + 1L,
                tableName = table,
                isSuccess = op != "ERROR",
                errorMessage = if (op == "ERROR") "SQLite error on query #$i" else null,
                affectedRows = if (op != "ERROR") 1L else null,
            )
        }

        prefilledLogs = DatabaseLogRecorder.logs.value
    }

    /**
     * Hot-path cost of recording a single database query log entry.
     * This measures the exact overhead added when app or driver logs a query in real time.
     */
    @Benchmark
    fun recordQueryLog() {
        DatabaseLogRecorder.record(
            databaseName = "shop_sample.db",
            sql = "SELECT * FROM products WHERE stock > 10;",
            durationMs = 2L,
            tableName = "products",
            affectedRows = 15L,
        )
    }

    /**
     * Cost of reading the current snapshot of database logs from StateFlow.
     * Simulates UI recomposition reads.
     */
    @Benchmark
    fun readLogsSnapshot(): List<DatabaseLogEntry> = DatabaseLogRecorder.logs.value

    /**
     * Cost of filtering logs by SELECT operations across prefilled history.
     */
    @Benchmark
    fun filterSelectLogs(): List<DatabaseLogEntry> = prefilledLogs.filter { it.operation == "SELECT" && it.isSuccess }

    /**
     * Cost of filtering logs by ERROR status across prefilled history.
     */
    @Benchmark
    fun filterErrorLogs(): List<DatabaseLogEntry> = prefilledLogs.filter { !it.isSuccess || it.operation == "ERROR" }

    /**
     * Cost of full-text searching logs across SQL strings and table names.
     */
    @Benchmark
    fun searchLogs(): List<DatabaseLogEntry> =
        prefilledLogs.filter {
            it.sql.contains("products", ignoreCase = true) ||
                (it.tableName?.contains("products", ignoreCase = true) == true)
        }

    /**
     * Cost of formatting a single log entry into plain text for copying.
     */
    @Benchmark
    fun formatSingleLogForCopy(): String = DatabaseFormatUtils.formatDatabaseLogForCopy(sampleLogEntry)

    /**
     * Cost of formatting hundreds of database logs into a single plain-text string for Copy All.
     */
    @Benchmark
    fun formatAllLogsForCopy(): String = DatabaseFormatUtils.formatDatabaseLogsForCopy(prefilledLogs)

    /**
     * Cost of converting a table row map into pretty JSON.
     */
    @Benchmark
    fun rowToJson(): String = DatabaseFormatUtils.rowToJson(sampleRow)

    /**
     * Cost of generating a SQL INSERT statement for a table row map.
     */
    @Benchmark
    fun rowToSqlInsert(): String = DatabaseFormatUtils.rowToSqlInsert("users", sampleRow)
}
