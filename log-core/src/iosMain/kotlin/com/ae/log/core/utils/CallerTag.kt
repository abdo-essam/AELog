package com.ae.log.core.utils

import platform.Foundation.NSThread

public actual fun callerTag(): String {
    val skip =
        listOf(
            "com.ae.log.core.",
            "com.ae.log.plugins.",
            "com.ae.log.AELog",
            "com.ae.log.LogProxy",
            "com.ae.log.LogProvider",
            "AELog.",
            "androidx.compose.",
            "kotlin.",
        )
    val frame =
        (NSThread.callStackSymbols as? List<*>)
            ?.drop(1)
            ?.firstOrNull { sym ->
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
