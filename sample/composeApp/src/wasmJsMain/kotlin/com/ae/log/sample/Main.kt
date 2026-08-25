package com.ae.log.sample

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    SampleState.initialize()
    ComposeViewport(document.body!!) {
        App()
    }
}
