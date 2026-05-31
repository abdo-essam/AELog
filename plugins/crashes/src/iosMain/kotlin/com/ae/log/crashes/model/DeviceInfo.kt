@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.ae.log.crashes.model

import platform.UIKit.UIDevice
import platform.Foundation.NSBundle

internal actual fun currentDeviceInfo(): DeviceInfo {
    val device = UIDevice.currentDevice
    val appVersion = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
        as? String ?: "unknown"
    val buildNumber = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion")
        as? String ?: "unknown"

    return DeviceInfo(
        model = device.model,
        osVersion = "${device.systemName} ${device.systemVersion}",
        appVersion = appVersion,
        buildNumber = buildNumber,
    )
}
