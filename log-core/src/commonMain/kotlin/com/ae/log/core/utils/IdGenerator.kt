package com.ae.log.core.utils

import kotlin.uuid.Uuid

public object IdGenerator {
    @OptIn(kotlin.uuid.ExperimentalUuidApi::class)
    public fun next(): String = Uuid.random().toString()
}
