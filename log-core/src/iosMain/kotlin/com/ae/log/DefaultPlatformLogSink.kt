package com.ae.log

import com.ae.log.plugins.log.model.LogSeverity

internal actual class DefaultPlatformLogSink : PlatformLogSink {
    actual override fun log(severity: LogSeverity, tag: String, message: String, throwable: Throwable?) {
        val msg = "[$severity] $tag: $message"
        if (throwable != null) println("$msg\n${throwable.stackTraceToString()}") else println(msg)
    }
}
