package com.ae.log.core.utils

public actual fun callerTag(): String {
    val skip = listOf("com.ae.log", "java.", "kotlin.", "androidx.compose.")
    return Throwable()
        .stackTrace
        .firstOrNull { frame -> skip.none { frame.className.startsWith(it) } }
        ?.className
        ?.substringAfterLast('.')
        ?.substringBefore('$') ?: "AELog"
}
