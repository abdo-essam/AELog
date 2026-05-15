package com.ae.log.utils

/**
 * Internal package prefixes that should never appear as a caller tag.
 *
 * Shared across all platform [callerTag] actuals so the skip list stays in
 * a single place and doesn't drift between Android, JVM, and iOS.
 */
internal val CALLER_TAG_SKIP_PREFIXES: List<String> =
    listOf(
        "com.ae.log.AELog",
        "com.ae.log.logs.LogProxy",
        "com.ae.log.LogInspector",
        "com.ae.log.logs.AELogLogsKt",
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

/**
 * Strips compiler-generated suffixes and normalises a raw class name into a
 * human-readable tag, e.g.:
 * - `"MyViewModel$lambda"` → `"MyViewModel"`
 * - `"ComposableSingletons$MyScreenKt"` → `"MyScreen"`
 * - `"UtilsKt"` → `"Utils"`
 *
 * Returns `"AELog"` if the result would be blank.
 */
internal fun normaliseClassName(rawClassName: String): String {
    val simple = rawClassName.substringAfterLast('.')
    val firstPart = simple.substringBefore('$')
    val tag =
        if (firstPart == "ComposableSingletons") {
            simple.substringAfter('$').substringBefore('$')
        } else {
            firstPart
        }.removeSuffix("Kt")
    return tag.ifBlank { null } ?: "AELog"
}
