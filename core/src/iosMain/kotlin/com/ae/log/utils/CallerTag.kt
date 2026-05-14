package com.ae.log.utils

import platform.Foundation.NSThread

public actual fun callerTag(): String {
    val frame =
        NSThread.callStackSymbols
            .drop(1)
            .firstOrNull { sym ->
                val symbolStr = sym as? String ?: return@firstOrNull false
                CALLER_TAG_SKIP_PREFIXES.none { symbolStr.contains(it) } &&
                    !symbolStr.contains("AELog.") &&
                    !symbolStr.contains("dalvik.")
            } as? String ?: return "AELog"

    val functionCall = frame.split(Regex("\\s+")).getOrNull(3) ?: return "AELog"
    val clean =
        functionCall
            .removePrefix("kfun:")
            .removePrefix("kclass:")
            .removePrefix("objc:")
            .substringBefore('(')

    val parts = clean.split('.')
    val rawClassName =
        parts.findLast { it.firstOrNull()?.isUpperCase() == true }
            ?: parts.getOrNull(parts.size - 2)
            ?: return "AELog"

    return normaliseClassName(rawClassName)
}
