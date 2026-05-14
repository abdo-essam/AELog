package com.ae.log.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Controls the visibility of the AELog UI overlay.
 *
 * Access via [LocalLogController] inside [com.ae.log.ui.LogProvider].
 */
public class LogController {
    private val _isVisible = MutableStateFlow(false)
    public val isVisible: StateFlow<Boolean> = _isVisible.asStateFlow()

    public val activeTabIndex: MutableStateFlow<Int> = MutableStateFlow(0)

    public fun show() {
        _isVisible.value = true
    }

    public fun hide() {
        _isVisible.value = false
    }

    public fun toggle() {
        _isVisible.update { !it }
    }
}

/**
 * CompositionLocal providing the [LogController].
 *
 * Available anywhere inside [com.ae.log.ui.LogProvider].
 */
public val LocalLogController: ProvidableCompositionLocal<LogController> =
    staticCompositionLocalOf {
        error("LogController not provided. Wrap your content with LogProvider.")
    }
