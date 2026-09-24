package com.ae.log.database.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.ui.theme.LogSpacing
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

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(4.dp))
            .padding(horizontal = LogSpacing.x1_5, vertical = 2.dp),
    ) {
        Text(
            text = operation.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
internal fun PrimaryKeyBadge(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(LogTheme.colors.primaryContainer, RoundedCornerShape(4.dp))
            .padding(horizontal = LogSpacing.x1_5, vertical = 2.dp),
    ) {
        Text(
            text = "PK",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = LogTheme.colors.onPrimaryContainer,
        )
    }
}

@Composable
internal fun NotNullBadge(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(LogTheme.colors.surfaceVariant, RoundedCornerShape(4.dp))
            .padding(horizontal = LogSpacing.x1_5, vertical = 2.dp),
    ) {
        Text(
            text = "NOT NULL",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = LogTheme.colors.onSurfaceVariant,
        )
    }
}

@Composable
internal fun UniqueIndexBadge(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color(0xFFE3F2FD), RoundedCornerShape(4.dp))
            .padding(horizontal = LogSpacing.x1_5, vertical = 2.dp),
    ) {
        Text(
            text = "UNIQUE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1565C0),
        )
    }
}
