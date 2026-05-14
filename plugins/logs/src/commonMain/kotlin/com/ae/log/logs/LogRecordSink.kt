package com.ae.log.logs

import com.ae.log.plugin.Plugin
import com.ae.log.logs.model.LogSeverity

/**
 * Implemented by plugins that want to receive log entries from [com.ae.log.AELog.log].
 * Breaks the direct [com.ae.log.AELog] → [com.ae.log.logs.LogPlugin] coupling.
 */
public interface LogRecordSink : Plugin {
    public fun record(
        severity: LogSeverity,
        tag: String,
        msg: String,
        throwable: Throwable?,
    )
}
