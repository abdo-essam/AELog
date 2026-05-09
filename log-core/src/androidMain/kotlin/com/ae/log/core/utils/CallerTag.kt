package com.ae.log.core.utils

internal actual fun callerTag(): String {
    val skip = listOf("com.ae.log", "java.", "kotlin.", "dalvik.", "android.")
    return Throwable()
        .stackTrace
        .firstOrNull { frame -> skip.none { frame.className.startsWith(it) } }
        ?.className
        ?.substringAfterLast('.')
        ?.substringBefore('$') ?: "AELog"
}
