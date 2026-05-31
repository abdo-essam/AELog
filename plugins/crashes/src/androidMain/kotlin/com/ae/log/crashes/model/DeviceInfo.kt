package com.ae.log.crashes.model

import android.os.Build
import com.ae.log.crashes.CrashAppContextHolder

internal actual fun currentDeviceInfo(): DeviceInfo {
    val ctx = CrashAppContextHolder.get()
    val appVersion =
        runCatching {
            ctx
                ?.packageManager
                ?.getPackageInfo(ctx.packageName, 0)
                ?.versionName ?: "unknown"
        }.getOrDefault("unknown")

    val buildNumber =
        runCatching {
            @Suppress("DEPRECATION")
            ctx
                ?.packageManager
                ?.getPackageInfo(ctx.packageName, 0)
                ?.versionCode
                ?.toString() ?: "unknown"
        }.getOrDefault("unknown")

    return DeviceInfo(
        model = "${Build.MANUFACTURER} ${Build.MODEL}".trim(),
        osVersion = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
        appVersion = appVersion,
        buildNumber = buildNumber,
    )
}
