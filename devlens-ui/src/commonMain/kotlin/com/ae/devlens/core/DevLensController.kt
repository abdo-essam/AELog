package com.ae.devlens.core

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls the visibility of the DevLens UI overlay.
 *
 * Access via [LocalDevLensController] inside [com.ae.devlens.AEDevLensProvider].
 */
public class DevLensController {
    private val _isVisible = MutableStateFlow(false)
    public val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    public fun show() { _isVisible.value = true }
    public fun hide() { _isVisible.value = false }
    public fun toggle() { _isVisible.value = !_isVisible.value }
}

/**
 * CompositionLocal providing the [DevLensController].
 *
 * Available anywhere inside [com.ae.devlens.AEDevLensProvider].
 */
public val LocalDevLensController: ProvidableCompositionLocal<DevLensController> = compositionLocalOf {
    error("DevLensController not provided. Wrap your content with AEDevLensProvider.")
}
