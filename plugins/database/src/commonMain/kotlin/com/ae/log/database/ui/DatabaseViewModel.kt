package com.ae.log.database.ui

import com.ae.log.database.DatabaseLogRecorder
import com.ae.log.database.config.DatabasePluginConfig
import com.ae.log.database.inspector.DatabaseInspector
import com.ae.log.database.inspector.isWriteStatement
import com.ae.log.database.model.DatabaseLogEntry
import com.ae.log.database.model.DatabaseLogFilter
import com.ae.log.database.model.DbInfo
import com.ae.log.database.model.DbTable
import com.ae.log.database.model.QueryResult
import com.ae.log.database.model.TableSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal sealed interface DatabaseDestination {
    data object DatabaseList : DatabaseDestination

    data class TablesList(
        val db: DbInfo,
    ) : DatabaseDestination

    data class TableData(
        val db: DbInfo,
        val table: DbTable,
    ) : DatabaseDestination

    data class RowDetails(
        val db: DbInfo,
        val table: DbTable,
        val row: Map<String, String?>,
        val rowIndex: Int = 0,
    ) : DatabaseDestination

    data class TableSchemaView(
        val db: DbInfo,
        val table: DbTable,
    ) : DatabaseDestination

    data class QueryEditor(
        val db: DbInfo,
        val initialSql: String = "",
    ) : DatabaseDestination

    data class DatabaseLogs(
        val db: DbInfo? = null,
    ) : DatabaseDestination
}

private const val SCHEMA_TAB_LABEL = "Schema"

internal enum class TablesTab(
    val label: String,
) {
    LOGS("Logs"),
    TABLES("Tables"),
    SCHEMA(SCHEMA_TAB_LABEL),
}

internal enum class TableDataTab(
    val label: String,
) {
    DATA("Data"),
    SCHEMA(SCHEMA_TAB_LABEL),
    QUERY("Query"),
}

internal class DatabaseViewModel(
    val inspector: DatabaseInspector,
    val config: DatabasePluginConfig,
    private val scope: CoroutineScope,
) {
    // ── Navigation Stack ──────────────────────────────────────────────
    private val _navigationStack = MutableStateFlow<List<DatabaseDestination>>(listOf(DatabaseDestination.DatabaseList))
    val navigationStack: StateFlow<List<DatabaseDestination>> = _navigationStack.asStateFlow()

    val currentDestination: StateFlow<DatabaseDestination> =
        combine(_navigationStack) { stack -> stack.firstOrNull()?.lastOrNull() ?: DatabaseDestination.DatabaseList }
            .stateIn(scope, SharingStarted.Eagerly, DatabaseDestination.DatabaseList)

    fun navigateTo(destination: DatabaseDestination) {
        val current = _navigationStack.value.toMutableList()
        current.add(destination)
        _navigationStack.value = current
    }

    fun popBack(): Boolean {
        val current = _navigationStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.lastIndex)
            _navigationStack.value = current
            return true
        }
        return false
    }

    // ── Database List State ───────────────────────────────────────────
    private val _databases = MutableStateFlow<List<DbInfo>>(emptyList())
    val databases: StateFlow<List<DbInfo>> = _databases.asStateFlow()

    private val _selectedDatabase = MutableStateFlow<DbInfo?>(null)
    val selectedDatabase: StateFlow<DbInfo?> = _selectedDatabase.asStateFlow()

    // ── Tables List State ─────────────────────────────────────────────
    private val _tables = MutableStateFlow<List<DbTable>>(emptyList())
    val tables: StateFlow<List<DbTable>> = _tables.asStateFlow()

    private val _tablesSearchQuery = MutableStateFlow("")
    val tablesSearchQuery: StateFlow<String> = _tablesSearchQuery.asStateFlow()

    private val _tablesTab = MutableStateFlow(TablesTab.LOGS)
    val tablesTab: StateFlow<TablesTab> = _tablesTab.asStateFlow()

    // ── Table Data State ──────────────────────────────────────────────
    private val _selectedTable = MutableStateFlow<DbTable?>(null)
    val selectedTable: StateFlow<DbTable?> = _selectedTable.asStateFlow()

    private val _tableDataTab = MutableStateFlow(TableDataTab.DATA)
    val tableDataTab: StateFlow<TableDataTab> = _tableDataTab.asStateFlow()

    private val _tableData = MutableStateFlow<QueryResult?>(null)
    val tableData: StateFlow<QueryResult?> = _tableData.asStateFlow()

    private val _tablePage = MutableStateFlow(0)
    val tablePage: StateFlow<Int> = _tablePage.asStateFlow()

    private val _tablePageSize = MutableStateFlow(10)
    val tablePageSize: StateFlow<Int> = _tablePageSize.asStateFlow()

    private val _tableSortColumn = MutableStateFlow<String?>(null)
    val tableSortColumn: StateFlow<String?> = _tableSortColumn.asStateFlow()

    private val _tableSortAscending = MutableStateFlow(true)
    val tableSortAscending: StateFlow<Boolean> = _tableSortAscending.asStateFlow()

    private val _tableDataSearchQuery = MutableStateFlow("")
    val tableDataSearchQuery: StateFlow<String> = _tableDataSearchQuery.asStateFlow()

    private val _isTableLoading = MutableStateFlow(false)
    val isTableLoading: StateFlow<Boolean> = _isTableLoading.asStateFlow()

    // ── Table Schema State ────────────────────────────────────────────
    private val _tableSchema = MutableStateFlow<TableSchema?>(null)
    val tableSchema: StateFlow<TableSchema?> = _tableSchema.asStateFlow()

    private val _isSchemaLoading = MutableStateFlow(false)
    val isSchemaLoading: StateFlow<Boolean> = _isSchemaLoading.asStateFlow()

    // ── Query Editor State ────────────────────────────────────────────
    private val _queryEditorSql = MutableStateFlow("")
    val queryEditorSql: StateFlow<String> = _queryEditorSql.asStateFlow()

    private val _queryEditorResult = MutableStateFlow<QueryResult?>(null)
    val queryEditorResult: StateFlow<QueryResult?> = _queryEditorResult.asStateFlow()

    private val _isQueryEditorRunning = MutableStateFlow(false)
    val isQueryEditorRunning: StateFlow<Boolean> = _isQueryEditorRunning.asStateFlow()

    // ── Database Logs State ───────────────────────────────────────────
    private val _logFilter = MutableStateFlow(DatabaseLogFilter.ALL)
    val logFilter: StateFlow<DatabaseLogFilter> = _logFilter.asStateFlow()

    private val _logSearchQuery = MutableStateFlow("")
    val logSearchQuery: StateFlow<String> = _logSearchQuery.asStateFlow()

    val logs: StateFlow<List<DatabaseLogEntry>> = DatabaseLogRecorder.logs

    val filteredLogs: StateFlow<List<DatabaseLogEntry>> =
        combine(
            DatabaseLogRecorder.logs,
            _logFilter,
            _logSearchQuery,
            _selectedDatabase,
        ) { allLogs, filter, search, currentDb ->
            allLogs.filter { entry ->
                val matchesFilter =
                    when (filter) {
                        DatabaseLogFilter.ALL -> true
                        DatabaseLogFilter.SELECTS -> entry.operation == "SELECT" && entry.isSuccess
                        DatabaseLogFilter.INSERTS -> entry.operation == "INSERT" && entry.isSuccess
                        DatabaseLogFilter.UPDATES -> entry.operation == "UPDATE" && entry.isSuccess
                        DatabaseLogFilter.DELETES -> entry.operation == "DELETE" && entry.isSuccess
                        DatabaseLogFilter.SCHEMA ->
                            entry.operation in listOf("CREATE", "DROP", "ALTER", "REPLACE") &&
                                entry.isSuccess
                        DatabaseLogFilter.ERRORS -> !entry.isSuccess || entry.operation == "ERROR"
                    }
                val matchesSearch =
                    search.isBlank() ||
                        entry.sql.contains(search, ignoreCase = true) ||
                        (entry.tableName?.contains(search, ignoreCase = true) == true) ||
                        (entry.errorMessage?.contains(search, ignoreCase = true) == true)

                matchesFilter && matchesSearch
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // ── Write Protection & Safety ─────────────────────────────────────
    private val _isWriteModeEnabled = MutableStateFlow(config.allowWrite)
    val isWriteModeEnabled: StateFlow<Boolean> = _isWriteModeEnabled.asStateFlow()

    private val _pendingWriteConfirmation = MutableStateFlow<String?>(null)
    val pendingWriteConfirmation: StateFlow<String?> = _pendingWriteConfirmation.asStateFlow()

    init {
        refreshDatabases()
    }

    // ── Database Actions ──────────────────────────────────────────────
    fun refreshDatabases() {
        scope.launch {
            val list = inspector.listDatabases()
            _databases.value = list
            val current = _selectedDatabase.value
            if (current == null || list.none { it.path == current.path }) {
                list.firstOrNull()?.let { selectDatabase(it, navigate = false) }
            } else {
                val updated = list.first { it.path == current.path }
                _selectedDatabase.value = updated
                loadTables(updated)
            }
        }
    }

    fun selectDatabase(
        dbInfo: DbInfo,
        navigate: Boolean = true,
    ) {
        _selectedDatabase.value = dbInfo
        loadTables(dbInfo)
        if (navigate) {
            navigateTo(DatabaseDestination.TablesList(dbInfo))
        }
    }

    private fun loadTables(dbInfo: DbInfo) {
        scope.launch(Dispatchers.Default) {
            val tbls = inspector.listTables(dbInfo)
            _tables.value = tbls
            // Update tableCount on selected dbInfo
            _selectedDatabase.value = dbInfo.copy(tableCount = tbls.count { !it.isSystemTable })
        }
    }

    fun setTablesTab(tab: TablesTab) {
        _tablesTab.value = tab
    }

    fun setTablesSearchQuery(query: String) {
        _tablesSearchQuery.value = query
    }

    // ── Table Data Actions ────────────────────────────────────────────
    fun selectTable(
        dbInfo: DbInfo,
        table: DbTable,
        navigate: Boolean = true,
    ) {
        _selectedDatabase.value = dbInfo
        _selectedTable.value = table
        _tablePage.value = 0
        _tableSortColumn.value = null
        _tableDataSearchQuery.value = ""
        loadTableData(dbInfo, table, page = 0)
        loadTableSchema(dbInfo, table)
        setQueryEditorSql("SELECT * FROM \"${table.name}\" LIMIT 20;")

        if (navigate) {
            navigateTo(DatabaseDestination.TableData(dbInfo, table))
        }
    }

    fun setTableDataTab(tab: TableDataTab) {
        _tableDataTab.value = tab
    }

    fun loadTableData(
        dbInfo: DbInfo? = null,
        table: DbTable? = null,
        page: Int = _tablePage.value,
        sortColumn: String? = _tableSortColumn.value,
        sortAscending: Boolean = _tableSortAscending.value,
        searchQuery: String? = _tableDataSearchQuery.value,
    ) {
        val targetDb = dbInfo ?: _selectedDatabase.value ?: return
        val targetTable = table ?: _selectedTable.value ?: return
        _isTableLoading.value = true
        _tablePage.value = page
        scope.launch(Dispatchers.Default) {
            val limit = _tablePageSize.value
            val offset = page * limit
            val result =
                inspector.getTableData(
                    dbInfo = targetDb,
                    tableName = targetTable.name,
                    offset = offset,
                    limit = limit,
                    sortColumn = sortColumn,
                    sortAscending = sortAscending,
                    searchQuery = searchQuery,
                )
            _tableData.value = result
            _isTableLoading.value = false
        }
    }

    fun setTableDataSearch(query: String) {
        _tableDataSearchQuery.value = query
        val db = _selectedDatabase.value ?: return
        val table = _selectedTable.value ?: return
        loadTableData(db, table, page = 0, searchQuery = query)
    }

    fun toggleSort(columnName: String) {
        val currentSort = _tableSortColumn.value
        val isAsc = _tableSortAscending.value
        val newAsc = if (currentSort == columnName) !isAsc else true
        _tableSortColumn.value = columnName
        _tableSortAscending.value = newAsc
        val db = _selectedDatabase.value ?: return
        val table = _selectedTable.value ?: return
        loadTableData(db, table, page = 0, sortColumn = columnName, sortAscending = newAsc)
    }

    fun setPageSize(size: Int) {
        _tablePageSize.value = size
        val db = _selectedDatabase.value ?: return
        val table = _selectedTable.value ?: return
        loadTableData(db, table, page = 0)
    }

    fun nextPage() {
        val db = _selectedDatabase.value ?: return
        val table = _selectedTable.value ?: return
        val current = _tablePage.value
        loadTableData(db, table, page = current + 1)
    }

    fun previousPage() {
        val db = _selectedDatabase.value ?: return
        val table = _selectedTable.value ?: return
        val current = _tablePage.value
        if (current > 0) {
            loadTableData(db, table, page = current - 1)
        }
    }

    // ── Table Schema Actions ──────────────────────────────────────────
    fun loadTableSchema(
        dbInfo: DbInfo? = null,
        table: DbTable? = null,
    ) {
        val targetDb = dbInfo ?: _selectedDatabase.value ?: return
        val targetTable = table ?: _selectedTable.value ?: return
        _isSchemaLoading.value = true
        scope.launch(Dispatchers.Default) {
            val schema = inspector.getSchema(targetDb, targetTable.name)
            _tableSchema.value = schema
            _isSchemaLoading.value = false
        }
    }

    // ── Row Details Actions ───────────────────────────────────────────
    fun openRowDetails(
        dbInfo: DbInfo,
        table: DbTable,
        columns: List<String>,
        rowValues: List<String?>,
        rowIndex: Int,
    ) {
        val rowMap =
            columns.indices.associate { i ->
                val col = columns[i]
                val value = rowValues.getOrNull(i)
                col to value
            }
        navigateTo(DatabaseDestination.RowDetails(dbInfo, table, rowMap, rowIndex))
    }

    // ── Query Editor Actions ──────────────────────────────────────────
    fun setQueryEditorSql(sql: String) {
        _queryEditorSql.value = sql
    }

    fun runQueryEditor(forceConfirmed: Boolean = false) {
        val db = _selectedDatabase.value ?: return
        val sql = _queryEditorSql.value.trim()
        if (sql.isBlank()) return

        val isWrite = isWriteStatement(sql)
        if (isWrite && !_isWriteModeEnabled.value) {
            _queryEditorResult.value =
                QueryResult.error(
                    "Write operations (INSERT, UPDATE, DELETE, etc.) are disabled. Enable Edit Mode to allow modifications.",
                )
            return
        }

        if (isWrite && !forceConfirmed) {
            _pendingWriteConfirmation.value = sql
            return
        }

        _pendingWriteConfirmation.value = null
        _isQueryEditorRunning.value = true

        scope.launch(Dispatchers.Default) {
            val result =
                inspector.query(
                    dbInfo = db,
                    sql = sql,
                    allowWrite = _isWriteModeEnabled.value,
                )
            _queryEditorResult.value = result
            _isQueryEditorRunning.value = false

            // If write succeeded, reload tables and current table data
            if (isWrite && result.isSuccess) {
                loadTables(db)
                _selectedTable.value?.let { loadTableData(db, it, page = _tablePage.value) }
            }
        }
    }

    fun confirmPendingWrite() {
        runQueryEditor(forceConfirmed = true)
    }

    fun cancelPendingWrite() {
        _pendingWriteConfirmation.value = null
    }

    fun toggleWriteMode() {
        _isWriteModeEnabled.value = !_isWriteModeEnabled.value
    }

    // ── Logs Actions ──────────────────────────────────────────────────
    fun setLogFilter(filter: DatabaseLogFilter) {
        _logFilter.value = filter
    }

    fun setLogSearchQuery(query: String) {
        _logSearchQuery.value = query
    }

    fun clearLogs() {
        DatabaseLogRecorder.clear()
    }

    fun clear() {
        _queryEditorResult.value = null
        _queryEditorSql.value = ""
        clearLogs()
    }
}
