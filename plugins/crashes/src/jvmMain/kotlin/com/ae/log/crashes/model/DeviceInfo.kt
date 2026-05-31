package com.ae.log.crashes.model

internal actual fun currentDeviceInfo(): DeviceInfo =
    DeviceInfo(
        model = "${System.getProperty("os.name")} (JVM)",
        osVersion = System.getProperty("os.version") ?: "unknown",
        appVersion = System.getProperty("java.version") ?: "unknown",
        buildNumber = "N/A",
    )
