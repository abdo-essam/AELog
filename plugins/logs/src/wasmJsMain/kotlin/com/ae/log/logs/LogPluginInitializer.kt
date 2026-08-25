package com.ae.log.logs

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Logs plugin on WebAssembly (wasmJs).
 *
 * This top-level property is evaluated when the WebAssembly module is loaded —
 * registering [LogPlugin] with AELog automatically (zero-config, same as Android and iOS).
 */
@OptIn(InternalAELogApi::class)
private val initLogPlugin =
    run {
        AELog.install(LogPlugin())
    }
