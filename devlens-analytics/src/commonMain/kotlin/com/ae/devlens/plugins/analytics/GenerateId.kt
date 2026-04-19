package com.ae.devlens.plugins.analytics

import kotlin.math.abs

internal fun generateId(): String = abs(kotlin.random.Random.nextLong()).toString(36)
