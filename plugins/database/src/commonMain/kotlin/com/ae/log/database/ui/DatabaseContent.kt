package com.ae.log.database.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ae.log.database.ui.components.DatabaseListScreen
import com.ae.log.database.ui.components.DatabaseLogsScreen
import com.ae.log.database.ui.components.QueryEditorView
import com.ae.log.database.ui.components.RowDetailsScreen
import com.ae.log.database.ui.components.TableDataScreen
import com.ae.log.database.ui.components.TableSchemaView
import com.ae.log.database.ui.components.TablesListScreen
import com.ae.log.database.ui.components.WriteConfirmDialog

@Composable
internal fun DatabaseContent(
    viewModel: DatabaseViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) {
        viewModel.refreshDatabases()
    }

    val databases by viewModel.databases.collectAsState()
    val navigationStack by viewModel.navigationStack.collectAsState()
    val pendingWriteConfirmation by viewModel.pendingWriteConfirmation.collectAsState()

    val currentDestination = navigationStack.lastOrNull() ?: DatabaseDestination.DatabaseList

    pendingWriteConfirmation?.let { sql ->
        WriteConfirmDialog(
            sql = sql,
            onConfirm = { viewModel.confirmPendingWrite() },
            onDismiss = { viewModel.cancelPendingWrite() },
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentDestination,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "DatabaseScreenTransition",
        ) { destination ->
            when (destination) {
                is DatabaseDestination.DatabaseList -> {
                    DatabaseListScreen(
                        viewModel = viewModel,
                        databases = databases,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is DatabaseDestination.TablesList -> {
                    TablesListScreen(
                        db = destination.db,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is DatabaseDestination.TableData -> {
                    TableDataScreen(
                        db = destination.db,
                        table = destination.table,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is DatabaseDestination.RowDetails -> {
                    RowDetailsScreen(
                        db = destination.db,
                        table = destination.table,
                        row = destination.row,
                        rowIndex = destination.rowIndex,
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is DatabaseDestination.TableSchemaView -> {
                    val tableSchema by viewModel.tableSchema.collectAsState()
                    val isSchemaLoading by viewModel.isSchemaLoading.collectAsState()
                    TableSchemaView(
                        table = destination.table,
                        schema = tableSchema,
                        isLoading = isSchemaLoading,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is DatabaseDestination.QueryEditor -> {
                    val queryEditorSql by viewModel.queryEditorSql.collectAsState()
                    val queryEditorResult by viewModel.queryEditorResult.collectAsState()
                    val isQueryEditorRunning by viewModel.isQueryEditorRunning.collectAsState()
                    val isWriteModeEnabled by viewModel.isWriteModeEnabled.collectAsState()

                    QueryEditorView(
                        db = destination.db,
                        viewModel = viewModel,
                        sqlQuery = queryEditorSql,
                        onSqlQueryChange = { viewModel.setQueryEditorSql(it) },
                        queryResult = queryEditorResult,
                        isRunning = isQueryEditorRunning,
                        isWriteModeEnabled = isWriteModeEnabled,
                        onToggleWriteMode = { viewModel.toggleWriteMode() },
                        onRunQuery = { viewModel.runQueryEditor() },
                        showBackButton = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is DatabaseDestination.DatabaseLogs -> {
                    DatabaseLogsScreen(
                        viewModel = viewModel,
                        targetDb = destination.db,
                        showBackButton = true,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}
