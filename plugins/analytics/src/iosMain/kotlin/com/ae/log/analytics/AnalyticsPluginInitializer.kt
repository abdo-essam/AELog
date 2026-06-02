package com.ae.log.analytics

import com.ae.log.AELog
import com.ae.log.InternalAELogApi

/**
 * Auto-initializer for the AELog Analytics plugin on iOS.
 *
 * Uses [@EagerInitialization][kotlin.native.EagerInitialization] so this property is evaluated
 * when the Kotlin/Native framework is loaded — before any user code runs — registering
 * [AnalyticsPlugin] with AELog automatically (zero-config, same as the Android ContentProvider initializer).
 */
@OptIn(InternalAELogApi::class, ExperimentalStdlibApi::class)
@Suppress("DEPRECATION") // @EagerInitialization is the only zero-config iOS init mechanism.
@EagerInitialization
private val initAnalyticsPlugin =
    run {
        AELog.install(AnalyticsPlugin())
    }
