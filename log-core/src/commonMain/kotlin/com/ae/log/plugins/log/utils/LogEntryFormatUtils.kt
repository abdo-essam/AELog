package com.ae.log.plugins.log.utils

import com.ae.log.plugins.log.model.LogEntry
import com.ae.log.plugins.log.model.cleanMessage

internal data class AnalyticsPreview(
    val title: String,
    val subtitle: String?,
)

internal fun LogEntry.getAnalyticsPreview(): AnalyticsPreview {
    val content = this.message.removePrefix("📊 EVENT: ").removePrefix("📄 PAGE: ")
    val parts = content.split("|", limit = 2)
    return AnalyticsPreview(
        title = parts.getOrNull(0)?.trim() ?: content,
        subtitle = parts.getOrNull(1)?.trim(),
    )
}

internal fun LogEntry.getCleanMessagePreview(maxLength: Int = 80): String =
    this.cleanMessage.take(maxLength).replace("\n", " ")
