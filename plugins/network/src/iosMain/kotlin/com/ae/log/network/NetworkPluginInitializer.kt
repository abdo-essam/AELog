package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@kotlin.native.EagerInitialization
private val initNetworkPlugin = run {
    AELog.install(NetworkPlugin())
}
