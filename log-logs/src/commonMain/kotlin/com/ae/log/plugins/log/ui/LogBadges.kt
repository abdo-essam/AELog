package com.ae.log.plugins.log.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.log.model.LogSeverity

/** Severity-coloured badge (V / D / I / W / E / A). */
@Composable
internal fun SeverityBadge(severity: LogSeverity) {
    val (bg, fg) = severityColors(severity)
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(bg)
                .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = severity.label,
            style = MaterialTheme.typography.labelSmall,
            color = fg,
        )
    }
}

private fun severityColors(severity: LogSeverity): Pair<Color, Color> =
    when (severity) {
        LogSeverity.VERBOSE -> Color(0xFFBDBDBD) to Color.White
        LogSeverity.DEBUG -> Color(0xFF64B5F6) to Color.White
        LogSeverity.INFO -> Color(0xFF4CAF50) to Color.White
        LogSeverity.WARN -> Color(0xFFFFA726) to Color.White
        LogSeverity.ERROR -> Color(0xFFEF5350) to Color.White
        LogSeverity.ASSERT -> Color(0xFF7B1FA2) to Color.White
    }

