package com.ae.log.logs

import com.ae.log.logs.model.LogSeverity

public interface PlatformLogSink {
    public fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable? = null,
    )

    public companion object {
        public val Default: PlatformLogSink = DefaultPlatformLogSink()

        /**
         * A no-op sink that discards all log output.
         * Use this in unit tests to avoid platform-specific side-effects (Logcat, println).
         */
        public val None: PlatformLogSink = object : PlatformLogSink {
            override fun log(severity: LogSeverity, tag: String, message: String, throwable: Throwable?) = Unit
        }
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
