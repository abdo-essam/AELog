package com.ae.log.analytics

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Analytics plugin on WebAssembly (wasmJs).
 *
 * This top-level property is evaluated when the WebAssembly module is loaded —
 * registering [AnalyticsPlugin] with AELog automatically (zero-config, same as Android and iOS).
 */
@OptIn(InternalAELogApi::class)
private val initAnalyticsPlugin =
    run {
        AELog.install(AnalyticsPlugin())
    }
