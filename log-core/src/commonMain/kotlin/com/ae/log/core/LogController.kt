package com.ae.log.core

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controls the visibility of the AELog UI overlay.
 *
 * Access via [LocalLogController] inside [com.ae.log.AELogProvider].
 */
public class LogController {
    private val _isVisible = MutableStateFlow(false)
    public val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    public fun show() {
        _isVisible.value = true
    }

    public fun hide() {
        _isVisible.value = false
    }

    public fun toggle() {
        _isVisible.value = !_isVisible.value
    }
}

/**
 * CompositionLocal providing the [LogController].
 *
 * Available anywhere inside [com.ae.log.AELogProvider].
 */
public val LocalLogController: ProvidableCompositionLocal<LogController> =
    compositionLocalOf {
        error("LogController not provided. Wrap your content with AELogProvider.")
    }
