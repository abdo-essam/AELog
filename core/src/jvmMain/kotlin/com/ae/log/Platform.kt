package com.ae.log

internal actual class Platform actual constructor() {
    actual val name: String = "Desktop JVM (${System.getProperty("os.name")})"
}
