package com.ae.devlens.core

import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls the visibility of the DevLens UI overlay.
 *
 * Access via [LocalDevLensController] inside a [com.ae.devlens.AEDevLensProvider].
 */
class DevLensController {
    private val _isVisible = MutableStateFlow(false)
    val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    fun show() {
        _isVisible.value = true
    }

    fun hide() {
        _isVisible.value = false
    }

    fun toggle() {
        _isVisible.value = !_isVisible.value
    }
}

/**
 * CompositionLocal providing the [DevLensController].
 *
 * Available inside [com.ae.devlens.AEDevLensProvider].
 */
val LocalDevLensController =
    compositionLocalOf<DevLensController> {
        error("DevLensController not provided. Wrap your content with AEDevLensProvider.")
    }
