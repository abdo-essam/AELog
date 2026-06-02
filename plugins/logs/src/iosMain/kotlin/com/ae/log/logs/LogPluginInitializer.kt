package com.ae.log.logs

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@kotlin.native.EagerInitialization
private val initLogPlugin = run {
    AELog.install(LogPlugin())
}
