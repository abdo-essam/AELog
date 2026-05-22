package com.ae.log.crashes

import com.ae.log.AELog
import kotlin.jvm.JvmStatic

/**
 * Extension property to access the crash reporting API.
 * Only available if the `:plugins:crashes` module is included.
 *
 * Usage:
 * ```kotlin
 * AELog.crashes.recordNonFatal(exception)
 * ```
 */
public val AELog.crashes: CrashProxy get() = CrashProxy

public object CrashProxy {
    /**
     * Records a non-fatal exception into the crash viewer.
     *
     * Silent no-op if [AELog.init] has not been called or [CrashPlugin] is not installed.
     */
    @JvmStatic
    public fun recordNonFatal(
        throwable: Throwable,
        threadName: String = "main",
    ) {
        AELog.getPlugin<CrashPlugin>()?.recordNonFatal(throwable, threadName)
    }
}
