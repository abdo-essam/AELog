package com.ae.log.logs

import com.ae.log.logs.model.LogSeverity

internal actual class DefaultPlatformLogSink : PlatformLogSink {
    actual override fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val stream = if (severity == LogSeverity.ERROR || severity == LogSeverity.ASSERT) System.err else System.out
        stream.println("[$severity] $tag: $message")
        throwable?.printStackTrace(stream)
    }
}
