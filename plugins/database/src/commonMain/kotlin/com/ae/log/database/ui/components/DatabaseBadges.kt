package com.ae.log.database.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ae.log.database.model.DatabaseOperation
import com.ae.log.ui.components.LogBadge
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun OperationBadge(
    operation: DatabaseOperation,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) =
        when (operation) {
            DatabaseOperation.SELECT -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
            DatabaseOperation.INSERT -> Color(0xFFFFF3E0) to Color(0xFFE65100)
            DatabaseOperation.UPDATE -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
            DatabaseOperation.DELETE, DatabaseOperation.DROP -> Color(0xFFFFEBEE) to Color(0xFFC62828)
            DatabaseOperation.CREATE -> Color(0xFFE3F2FD) to Color(0xFF1565C0)
            DatabaseOperation.ALTER, DatabaseOperation.REPLACE -> Color(0xFFEDE7F6) to Color(0xFF512DA8)
            DatabaseOperation.PRAGMA, DatabaseOperation.TRANSACTION -> LogTheme.colors.surfaceVariant to LogTheme.colors.onSurfaceVariant
            DatabaseOperation.ERROR -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
            DatabaseOperation.OTHER -> LogTheme.colors.surfaceVariant to LogTheme.colors.onSurfaceVariant
        }

    LogBadge(
        text = operation.name,
        containerColor = bgColor,
        contentColor = textColor,
        modifier = modifier,
    )
}

@Composable
internal fun PrimaryKeyBadge(modifier: Modifier = Modifier) {
    LogBadge(
        text = "PK",
        containerColor = LogTheme.colors.primaryContainer,
        contentColor = LogTheme.colors.onPrimaryContainer,
        modifier = modifier,
    )
}

@Composable
internal fun NotNullBadge(modifier: Modifier = Modifier) {
    LogBadge(
        text = "NOT NULL",
        containerColor = LogTheme.colors.surfaceVariant,
        contentColor = LogTheme.colors.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
internal fun UniqueIndexBadge(modifier: Modifier = Modifier) {
    LogBadge(
        text = "UNIQUE",
        containerColor = Color(0xFFE3F2FD),
        contentColor = Color(0xFF1565C0),
        modifier = modifier,
    )
}

@Composable
internal fun ForeignKeyBadge(
    target: String? = null,
    modifier: Modifier = Modifier,
) {
    LogBadge(
        text = if (target.isNullOrBlank()) "FK" else "FK → $target",
        containerColor = Color(0xFFF3E5F5),
        contentColor = Color(0xFF7B1FA2),
        modifier = modifier,
    )
}
