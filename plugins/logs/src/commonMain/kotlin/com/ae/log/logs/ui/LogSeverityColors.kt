package com.ae.log.logs.ui

import androidx.compose.ui.graphics.Color
import com.ae.log.logs.model.LogSeverity

/**
 * Centralized severity color palette for the log viewer.
 *
 * Ensures visual consistency across [LogBadges] and any future
 * components that need to colour-code log severity levels.
 */
internal object LogSeverityColors {
    val VERBOSE_BG = Color(0xFFBDBDBD)
    val DEBUG_BG = Color(0xFF64B5F6)
    val INFO_BG = Color(0xFF4CAF50)
    val WARN_BG = Color(0xFFFFA726)
    val ERROR_BG = Color(0xFFEF5350)
    val ASSERT_BG = Color(0xFF7B1FA2)
    val ON_SEVERITY = Color.White

    fun backgroundFor(severity: LogSeverity): Color =
        when (severity) {
            LogSeverity.VERBOSE -> VERBOSE_BG
            LogSeverity.DEBUG -> DEBUG_BG
            LogSeverity.INFO -> INFO_BG
            LogSeverity.WARN -> WARN_BG
            LogSeverity.ERROR -> ERROR_BG
            LogSeverity.ASSERT -> ASSERT_BG
        }
}
