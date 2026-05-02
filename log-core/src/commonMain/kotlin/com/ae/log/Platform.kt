package com.ae.log

/** Platform-specific utilities. */
internal expect class Platform() {
    val name: String
}
