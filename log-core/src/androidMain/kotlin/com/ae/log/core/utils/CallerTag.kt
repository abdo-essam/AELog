package com.ae.log.core.utils

internal actual fun callerTag(): String {
    val skip = listOf(
        "com.ae.log.core.", "com.ae.log.plugins.", "com.ae.log.AELog", 
        "com.ae.log.LogProxy", "com.ae.log.DefaultPlatformLogSink", 
        "com.ae.log.LogProvider", "java.", "kotlin.", "dalvik.", "android.", "androidx.compose."
    )
    val className = Throwable()
        .stackTrace
        .firstOrNull { frame -> skip.none { frame.className.startsWith(it) } }
        ?.className
        ?.substringAfterLast('.') ?: return "AELog"

    val firstPart = className.substringBefore('$')
    val tag = if (firstPart == "ComposableSingletons") {
        className.substringAfter('$').substringBefore('$')
    } else {
        firstPart
    }.removeSuffix("Kt")

    return tag.ifBlank { null } ?: "AELog"
}
