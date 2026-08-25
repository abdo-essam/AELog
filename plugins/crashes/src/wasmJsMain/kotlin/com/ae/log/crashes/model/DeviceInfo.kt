@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.ae.log.crashes.model

/**
 * Wasm/JS actual for [currentDeviceInfo].
 *
 * Reads browser / OS metadata from `navigator.userAgent` and related JS
 * properties via [JsFun] interop.  The values are best-effort — user-agent
 * strings vary by browser and can be spoofed.
 */
internal actual fun currentDeviceInfo(): DeviceInfo =
    DeviceInfo(
        model = jsNavigatorPlatform(),
        osVersion = jsNavigatorUserAgent(),
        appVersion = "web",
        buildNumber = "N/A",
    )

@JsFun("() => (typeof navigator !== 'undefined' && navigator.platform) ? navigator.platform : 'Browser'")
private external fun jsNavigatorPlatform(): String

@JsFun("() => (typeof navigator !== 'undefined' && navigator.userAgent) ? navigator.userAgent : 'unknown'")
private external fun jsNavigatorUserAgent(): String
