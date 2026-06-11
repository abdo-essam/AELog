package com.ae.log.crashes

internal actual fun defaultCrashStorageDir(): String = "${System.getProperty("user.home")}/.ae_crashes"
