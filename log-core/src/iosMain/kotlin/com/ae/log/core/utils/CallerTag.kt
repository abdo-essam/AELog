package com.ae.log.core.utils

import platform.Foundation.NSThread

internal actual fun callerTag(): String {
    val skip = listOf("com.ae.log", "AELog")
    val frame =
        (NSThread.callStackSymbols as? List<*>)
            ?.drop(1)
            ?.firstOrNull { sym -> skip.none { (sym as? String)?.contains(it) == true } } as? String
    return frame
        ?.split(Regex("\\s+"))
        ?.getOrNull(3)
        ?.substringBefore('.')
        ?.ifBlank { null } ?: "AELog"
}
