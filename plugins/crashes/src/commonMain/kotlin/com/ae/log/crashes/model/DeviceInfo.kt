package com.ae.log.crashes.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

/**
 * Snapshot of device / OS metadata captured at crash time.
 *
 * Populated via [currentDeviceInfo] — an `expect/actual` function so each
 * platform fills in real values (e.g. `android.os.Build` on Android).
 */
@Immutable
@Serializable
public data class DeviceInfo(
    /** Human-readable device model, e.g. "Pixel 8 Pro". */
    val model: String,
    /** OS name + version string, e.g. "Android 14 (API 34)". */
    val osVersion: String,
    /** App version name from the manifest / bundle, e.g. "2.3.1". */
    val appVersion: String,
    /** App build number / version code, e.g. "412". */
    val buildNumber: String,
)

/** Returns a [DeviceInfo] snapshot for the current platform. */
internal expect fun currentDeviceInfo(): DeviceInfo
