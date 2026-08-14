package com.ae.log.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.ae.log.ui.theme.LogThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * CompositionLocal for the [LogController].
 */
internal val LocalLogController = staticCompositionLocalOf<LogController> {
    error("No LogController provided")
}

/**
 * Controls the visibility of the AELog UI overlay internally.
 */
internal class LogController internal constructor(
    private val backing: MutableStateFlow<Boolean>,
    private val themeBacking: MutableStateFlow<LogThemeMode> = MutableStateFlow(LogThemeMode.SYSTEM),
) {
    /** Creates a standalone controller backed by its own private [MutableStateFlow]. */
    constructor() : this(MutableStateFlow(false), MutableStateFlow(LogThemeMode.SYSTEM))

    val isVisible: StateFlow<Boolean> = backing.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible: StateFlow<Boolean> = _isSettingsVisible.asStateFlow()

    val themeMode: StateFlow<LogThemeMode> = themeBacking.asStateFlow()

    fun show() {
        backing.value = true
    }

    fun hide() {
        backing.value = false
    }

    fun toggle() {
        backing.update { !it }
    }

    fun selectTab(index: Int) {
        _activeTabIndex.value = index.coerceAtLeast(0)
        _isSettingsVisible.value = false
    }

    fun showSettings() {
        _isSettingsVisible.value = true
    }

    fun hideSettings() {
        _isSettingsVisible.value = false
    }

    fun setThemeMode(mode: LogThemeMode) {
        themeBacking.value = mode
    }
}
