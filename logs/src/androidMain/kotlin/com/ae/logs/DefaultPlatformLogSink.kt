package com.ae.logs

import android.util.Log
import com.ae.logs.plugins.logs.model.LogSeverity

internal actual class DefaultPlatformLogSink : PlatformLogSink {
    override fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val priority =
            when (severity) {
                LogSeverity.VERBOSE -> Log.VERBOSE
                LogSeverity.DEBUG -> Log.DEBUG
                LogSeverity.INFO -> Log.INFO
                LogSeverity.WARN -> Log.WARN
                LogSeverity.ERROR -> Log.ERROR
                LogSeverity.ASSERT -> Log.ASSERT
            }
        if (throwable != null) {
            Log.println(priority, tag, "$message\n${Log.getStackTraceString(throwable)}")
        } else {
            Log.println(priority, tag, message)
        }
    }
}
