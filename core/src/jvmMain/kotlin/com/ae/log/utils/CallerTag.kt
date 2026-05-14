package com.ae.log.utils

public actual fun callerTag(): String {
    val className =
        Throwable()
            .stackTrace
            .firstOrNull { frame -> CALLER_TAG_SKIP_PREFIXES.none { frame.className.startsWith(it) } }
            ?.className ?: return "AELog"
    return normaliseClassName(className)
}
