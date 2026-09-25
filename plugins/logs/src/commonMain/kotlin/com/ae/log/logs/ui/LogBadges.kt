package com.ae.log.logs.ui

import androidx.compose.runtime.Composable
import com.ae.log.logs.model.LogSeverity
import com.ae.log.ui.components.LogBadge

/** Severity-coloured badge (V / D / I / W / E / A). */
@Composable
internal fun SeverityBadge(severity: LogSeverity) {
    val bg = LogSeverityColors.backgroundFor(severity)
    LogBadge(
        text = severity.label,
        containerColor = bg,
        contentColor = LogSeverityColors.ON_SEVERITY,
    )
}
