package com.ae.log

/**
 * Marks APIs that are intended exclusively for use in unit tests.
 *
 * These APIs are public to allow cross-module test access, but must **never**
 * be called from production code.
 *
 * Opt in with:
 * ```kotlin
 * @OptIn(AELogTestApi::class)
 * ```
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This API is intended for use in unit tests only. Do not call from production code.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
public annotation class AELogTestApi

/**
 * Marks APIs that are internal to the AELog system.
 *
 * These APIs are public to allow cross-module access (e.g. from plugin modules),
 * but must **never** be called from application code.
 *
 * Opt in with:
 * ```kotlin
 * @OptIn(InternalAELogApi::class)
 * ```
 */
@RequiresOptIn(
    level = RequiresOptIn.Level.ERROR,
    message = "This is an internal AELog API. It should not be used in application code.",
)
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
public annotation class InternalAELogApi
