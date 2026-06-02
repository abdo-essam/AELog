package com.ae.log.crashes

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@kotlin.native.EagerInitialization
private val initCrashPlugin = run {
    AELog.install(CrashPlugin())
}
