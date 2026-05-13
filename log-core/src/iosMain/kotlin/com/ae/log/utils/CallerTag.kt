package com.ae.log.utils

import platform.Foundation.NSThread

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
            "AELog.",
            "androidx.compose.",
            "kotlin.",
        )
    val frame =
        NSThread.callStackSymbols
            .drop(1)
            .firstOrNull { sym ->
                val symbolStr = sym as? String ?: return@firstOrNull false
                skip.none { symbolStr.contains(it) }
            } as? String

    val functionCall = frame?.split(Regex("\\s+"))?.getOrNull(3) ?: return "AELog"
    val clean =
        functionCall
            .removePrefix("kfun:")
            .removePrefix("kclass:")
            .removePrefix("objc:")
            .substringBefore('(')

    val parts = clean.split('.')
    val className =
        parts.findLast { it.firstOrNull()?.isUpperCase() == true }
            ?: parts.getOrNull(parts.size - 2)
            ?: return "AELog"

    val firstPart = className.substringBefore('$')
    val tag =
        if (firstPart == "ComposableSingletons") {
            className.substringAfter('$').substringBefore('$')
        } else {
            firstPart
        }.removeSuffix("Kt")

    return tag.ifBlank { null } ?: "AELog"
}
