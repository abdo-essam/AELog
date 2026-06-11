package com.ae.log.crashes.model

/**
 * Controls which crash events are visible based on severity.
 */
public enum class CrashFilter(
    public val label: String,
) {
    ALL("All"),
    FATAL("Fatal"),
    NON_FATAL("Non-Fatal"),
    ;

    public fun matches(event: CrashEvent): Boolean =
        when (this) {
            ALL -> true
            FATAL -> event.isFatal
            NON_FATAL -> !event.isFatal
        }
}
