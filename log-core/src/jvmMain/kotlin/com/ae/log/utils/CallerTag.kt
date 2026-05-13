package com.ae.log.utils

public actual fun callerTag(): String {
    val skip =
        listOf(
            "com.ae.log.AELog",
            "com.ae.log.LogProxy",
            "com.ae.log.LogInspector",
            "com.ae.log.AELogLogsKt",
            "com.ae.log.config.",
            "com.ae.log.event.",
            "com.ae.log.storage.",
            "com.ae.log.utils.",
            "com.ae.log.plugin.",
            "com.ae.log.ui.",
            "com.ae.log.plugins.",
            "java.",
            "kotlin.",
            "androidx.compose.",
        )
    val className =
        Throwable()
            .stackTrace
            .firstOrNull { frame -> skip.none { frame.className.startsWith(it) } }
            ?.className
            ?.substringAfterLast('.') ?: return "AELog"

    val firstPart = className.substringBefore('$')
    val tag =
        if (firstPart == "ComposableSingletons") {
            className.substringAfter('$').substringBefore('$')
        } else {
            firstPart
        }.removeSuffix("Kt")

    return tag.ifBlank { null } ?: "AELog"
}
