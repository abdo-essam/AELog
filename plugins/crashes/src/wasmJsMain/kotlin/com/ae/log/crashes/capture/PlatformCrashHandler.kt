@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class, kotlin.js.ExperimentalJsExport::class)

package com.ae.log.crashes.capture

/**
 * Wasm/JS actual for [PlatformCrashHandler].
 *
 * Installs a `window.onerror` listener that wraps the raw JS error message and
 * stack into a synthetic [RuntimeException] and forwards it to [CrashRecorder].
 *
 * ### Limitations
 * - The JS `Error` object is not a Kotlin [Throwable]; message and stack trace
 *   strings are captured and re-packaged as best-effort text.
 * - `window.onerror` does not fire for Promise rejections — for those,
 *   `window.onunhandledrejection` would be needed (not yet wired here).
 * - The handler cannot prevent the browser from also logging the error to the
 *   console, which is the expected browser behaviour.
 */
internal actual class PlatformCrashHandler actual constructor(
    private val recorder: CrashRecorder,
) {
    actual fun install() {
        installWindowOnError(recorder)
    }

    actual fun uninstall() {
        clearWindowOnError()
    }
}

/**
 * Stores a reference to the active [CrashRecorder] so the JS callback (which
 * runs outside Kotlin's call stack) can reach it.
 */
private var activeRecorder: CrashRecorder? = null

@JsFun(
    """() => {
        var _prev = window.onerror;
        window.onerror = function(message, source, line, col, error) {
            var stack = (error && error.stack) ? error.stack : (source + ':' + line + ':' + col);
            if (window.__aelogOnError) window.__aelogOnError(String(message), String(stack));
            if (_prev) return _prev(message, source, line, col, error);
            return false;
        };
    }""",
)
private external fun jsInstallOnError()

@JsFun("() => { window.onerror = null; window.__aelogOnError = null; }")
private external fun jsClearOnError()

/** Called from the JS `window.onerror` shim above via [registerKotlinCallback]. */
@JsExport
public fun onJsUncaughtError(
    message: String,
    stack: String,
) {
    val throwable = RuntimeException("$message\n$stack")
    activeRecorder?.record(
        throwable = throwable,
        threadName = "main",
        isFatal = true,
    )
}

@JsFun(
    """() => {
        window.__aelogOnError = function(msg, stack) {
            // Bridge back into Kotlin — the function name is mangled by the compiler;
            // we use the stable @JsExport name resolved at runtime.
            if (typeof com !== 'undefined' &&
                com.ae && com.ae.log && com.ae.log.crashes && com.ae.log.crashes.capture) {
                com.ae.log.crashes.capture.onJsUncaughtError(msg, stack);
            }
        };
    }""",
)
private external fun jsRegisterKotlinCallback()

private fun installWindowOnError(recorder: CrashRecorder) {
    activeRecorder = recorder
    jsRegisterKotlinCallback()
    jsInstallOnError()
}

private fun clearWindowOnError() {
    activeRecorder = null
    jsClearOnError()
}
