package com.ae.log.analytics

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@kotlin.native.EagerInitialization
private val initAnalyticsPlugin = run {
    AELog.install(AnalyticsPlugin())
}
