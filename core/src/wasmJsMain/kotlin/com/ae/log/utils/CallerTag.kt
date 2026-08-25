package com.ae.log.utils

/**
 * Wasm/JS actual for [callerTag].
 *
 * The Wasm/JS runtime does not expose a reflection-based stack-trace API
 * like JVM or Kotlin/Native.  We return the library name as a safe fallback.
 */
public actual fun callerTag(): String = "AELog"
