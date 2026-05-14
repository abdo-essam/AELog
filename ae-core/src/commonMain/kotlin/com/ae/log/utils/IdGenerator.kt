package com.ae.log.utils

import kotlin.uuid.Uuid

public object IdGenerator {
    public fun next(): String = Uuid.random().toString()
}
