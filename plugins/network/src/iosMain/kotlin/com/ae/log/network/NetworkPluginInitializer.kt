package com.ae.log.network

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Network plugin on iOS.
 *
 * Uses [@EagerInitialization][kotlin.native.EagerInitialization] so this property is evaluated
 * when the Kotlin/Native framework is loaded — before any user code runs — registering
 * [NetworkPlugin] with AELog automatically (zero-config, same as the Android ContentProvider initializer).
 */
@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@Suppress("DEPRECATION") // @EagerInitialization is the only zero-config iOS init mechanism.
@EagerInitialization
private val initNetworkPlugin =
    run {
        AELog.install(NetworkPlugin())
    }
