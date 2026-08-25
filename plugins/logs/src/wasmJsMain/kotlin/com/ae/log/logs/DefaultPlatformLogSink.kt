@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.ae.log.logs

import com.ae.log.logs.model.LogSeverity

/**
 * Wasm/JS implementation of [DefaultPlatformLogSink].
 *
 * Routes log output to the browser's developer console, preserving severity:
 * - [LogSeverity.ERROR] / [LogSeverity.ASSERT] → `console.error`
 * - [LogSeverity.WARN] → `console.warn`
 * - All other severities → `console.log`
 */
internal actual class DefaultPlatformLogSink : PlatformLogSink {
    actual override fun log(
        severity: LogSeverity,
        tag: String,
        message: String,
        throwable: Throwable?,
    ) {
        val msg = "[$severity] $tag: $message"
        val full = if (throwable != null) "$msg\n${throwable.stackTraceToString()}" else msg
        when (severity) {
            LogSeverity.ERROR, LogSeverity.ASSERT -> jsConsoleError(full)
            LogSeverity.WARN -> jsConsoleWarn(full)
            else -> jsConsoleLog(full)
        }
    }
}

@JsFun("(msg) => console.log(msg)")
private external fun jsConsoleLog(msg: String)

@JsFun("(msg) => console.warn(msg)")
private external fun jsConsoleWarn(msg: String)

@JsFun("(msg) => console.error(msg)")
private external fun jsConsoleError(msg: String)
