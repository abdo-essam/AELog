package com.ae.log.sample

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() =
    ComposeUIViewController(
        configure = {
            enforceStrictPlistSanityCheck = false
            onFocusBehavior = OnFocusBehavior.DoNothing
        },
    ) {
        SampleState.initialize()
        App()
    }
