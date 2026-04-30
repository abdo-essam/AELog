package com.ae.logs

import com.ae.logs.plugins.logs.model.LogSeverity

internal actual class DefaultPlatformLogSink : PlatformLogSink {
    override fun log(
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
