package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Network plugin on WebAssembly (wasmJs).
 *
 * This top-level property is evaluated when the WebAssembly module is loaded —
 * registering [NetworkPlugin] with AELog automatically (zero-config, same as Android and iOS).
 */
@OptIn(InternalAELogApi::class)
private val initNetworkPlugin =
    run {
        AELog.install(NetworkPlugin())
    }
