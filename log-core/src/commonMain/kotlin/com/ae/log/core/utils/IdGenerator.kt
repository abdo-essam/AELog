package com.ae.log.core.utils

import kotlin.uuid.Uuid

/**
 * Centralized ID generation for AELog.
 */
public object IdGenerator {
    /**
     * Generates a unique, collision-resistant string ID.
     */
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    public fun generateId(): String = Uuid.random().toString()
}
