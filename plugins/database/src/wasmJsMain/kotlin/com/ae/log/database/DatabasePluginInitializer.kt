package com.ae.log.database

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Database plugin on WebAssembly (wasmJs).
 *
 * This top-level property is evaluated when the WebAssembly module is loaded —
 * registering [DatabasePlugin] with AELog automatically (zero-config, same as Android and iOS).
 */
@OptIn(InternalAELogApi::class)
private val initDatabasePlugin =
    run {
        AELog.install(DatabasePlugin())
    }
