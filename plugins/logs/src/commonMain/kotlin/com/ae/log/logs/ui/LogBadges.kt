package com.ae.log.logs.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.ae.log.logs.model.LogSeverity
import com.ae.log.ui.theme.LogSpacing

/** Severity-coloured badge (V / D / I / W / E / A). */
@Composable
internal fun SeverityBadge(severity: LogSeverity) {
    val bg = LogSeverityColors.backgroundFor(severity)
    Box(
        modifier =
            Modifier
                .clip(RoundedCornerShape(LogSpacing.x1_5))
                .background(bg)
                .padding(horizontal = LogSpacing.x2, vertical = LogSpacing.x1),
    ) {
        Text(
            text = severity.label,
            style = MaterialTheme.typography.labelSmall,
            color = LogSeverityColors.ON_SEVERITY,
        )
    }
}
