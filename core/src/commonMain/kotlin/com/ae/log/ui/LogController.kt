package com.ae.log.ui

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Controls the visibility of the AELog UI overlay internally.
 */
internal class LogController internal constructor(
    private val backing: MutableStateFlow<Boolean>,
) {
    /** Creates a standalone controller backed by its own private [MutableStateFlow]. */
    constructor() : this(MutableStateFlow(false))

    val isVisible: StateFlow<Boolean> = backing.asStateFlow()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

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
    }
}

/**
 * CompositionLocal providing the [LogController] internally.
 */
internal val LocalLogController: ProvidableCompositionLocal<LogController> =
    staticCompositionLocalOf {
        error("LogController not provided.")
    }
