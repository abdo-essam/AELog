package com.ae.log

import com.ae.log.plugins.log.model.LogSeverity

public interface PlatformLogSink {
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    public companion object {
        public val Default: PlatformLogSink = DefaultPlatformLogSink()
    }
}

internal expect class DefaultPlatformLogSink() : PlatformLogSink {
    override fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable?,
    )
}
