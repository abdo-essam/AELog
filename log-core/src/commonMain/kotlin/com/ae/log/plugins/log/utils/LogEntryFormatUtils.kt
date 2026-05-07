package com.ae.log.plugins.log.utils

import com.ae.log.plugins.log.model.LogEntry

internal fun LogEntry.getCleanMessagePreview(maxLength: Int = 80): String =
    this.message.lines().firstOrNull()?.trim()?.take(maxLength) ?: ""
