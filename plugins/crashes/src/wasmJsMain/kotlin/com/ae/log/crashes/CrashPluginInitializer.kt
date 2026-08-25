package com.ae.log.crashes

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Crash plugin on WebAssembly (wasmJs).
 *
 * This top-level property is evaluated when the WebAssembly module is loaded —
 * registering [CrashPlugin] with AELog automatically (zero-config, same as Android and iOS).
 */
@OptIn(InternalAELogApi::class)
private val initCrashPlugin =
    run {
        AELog.install(CrashPlugin())
    }
