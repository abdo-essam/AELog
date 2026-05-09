package com.ae.log.core.utils

import kotlin.uuid.Uuid

public object IdGenerator {
    public fun next(): String = Uuid.random().toString()
}
