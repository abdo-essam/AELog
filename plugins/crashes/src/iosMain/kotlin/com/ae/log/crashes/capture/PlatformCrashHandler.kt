@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)

package com.ae.log.crashes.capture

import kotlin.concurrent.atomics.AtomicReference

/**
 * iOS/Native actual: installs a [kotlin.native.setUnhandledExceptionHook] handler.
 *
 * On Kotlin/Native the hook does NOT prevent the process from terminating —
 * we record synchronously before the runtime tears down.
 * runCatching guards the record() call for the same reason as Android/JVM.
 */
internal actual class PlatformCrashHandler actual constructor(
    private val recorder: CrashRecorder,
) {
    private val previousHook = AtomicReference<ReportUnhandledExceptionHook?>(null)

    actual fun install() {
        val previous =
            setUnhandledExceptionHook { throwable ->
                runCatching {
                    recorder.record(
                        throwable = throwable,
                        threadName = "main",
                        isFatal = true,
                    )
                }
                previousHook.value?.invoke(throwable)
            }
        previousHook.value = previous
    }

    actual fun uninstall() {
        val prev = previousHook.value
        setUnhandledExceptionHook(if (prev != null) prev else null)
        previousHook.value = null
    }
}
