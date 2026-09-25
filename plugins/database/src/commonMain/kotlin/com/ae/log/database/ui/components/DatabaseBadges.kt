package com.ae.log.database.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.ae.log.ui.components.LogBadge
import com.ae.log.ui.theme.LogTheme

@Composable
internal fun OperationBadge(
    operation: String,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (operation.uppercase()) {
        "SELECT" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "INSERT" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "UPDATE" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        "DELETE" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        "ERROR" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> LogTheme.colors.surfaceVariant to LogTheme.colors.onSurfaceVariant
    }

    LogBadge(
        text = operation.uppercase(),
        containerColor = bgColor,
        contentColor = textColor,
        modifier = modifier,
    )
}

@Composable
internal fun PrimaryKeyBadge(
    modifier: Modifier = Modifier,
) {
    LogBadge(
        text = "PK",
        containerColor = LogTheme.colors.primaryContainer,
        contentColor = LogTheme.colors.onPrimaryContainer,
        modifier = modifier,
    )
}

@Composable
internal fun NotNullBadge(
    modifier: Modifier = Modifier,
) {
    LogBadge(
        text = "NOT NULL",
        containerColor = LogTheme.colors.surfaceVariant,
        contentColor = LogTheme.colors.onSurfaceVariant,
        modifier = modifier,
    )
}

@Composable
internal fun UniqueIndexBadge(
    modifier: Modifier = Modifier,
) {
    LogBadge(
        text = "UNIQUE",
        containerColor = Color(0xFFE3F2FD),
        contentColor = Color(0xFF1565C0),
        modifier = modifier,
    )
}
