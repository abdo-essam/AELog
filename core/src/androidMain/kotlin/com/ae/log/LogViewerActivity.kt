package com.ae.log

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.ae.log.ui.LocalLogController
import com.ae.log.ui.LogProvider
import com.ae.log.ui.UiConfig
import kotlinx.coroutines.flow.first

/**
 * A dedicated Activity to show the Log interface in View-based Android apps.
 *
 * Launch this from anywhere in your app:
 * ```kotlin
 * startActivity(Intent(context, LogViewerActivity::class.java))
 * ```
 */
public class LogViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This makes sure the background is somewhat transparent or just black
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            LogProvider(
                uiConfig = UiConfig(showFloatingButton = false),
                enabled = true,
                content = {
                    val controller = LocalLogController.current

                    LaunchedEffect(Unit) {
                        controller.show()

                        // Wait until the state actually reflects that it is visible
                        controller.isVisible.first { it }

                        // Now wait until it becomes invisible (e.g. user closed it)
                        controller.isVisible.first { !it }

                        // Finish the Activity
                        finish()
                    }

                    // Empty container - the panel will open over it
                    Box(modifier = Modifier.fillMaxSize())
                },
            )
        }
    }
}

/**
 * Convenience helper to launch the UI directly from Android code.
 * This injects a ComposeView directly into the current Activity's DecorView,
 * avoiding any Activity lifecycle interruptions (no onPause/onResume refresh bugs).
 */
public fun AELog.launchViewer(context: android.content.Context) {
    val activity = context as? android.app.Activity ?: return
    val decorView = activity.window.decorView as android.view.ViewGroup

    // Check if we already added the viewer to avoid duplicates
    if (decorView.findViewById<android.view.View>(VIEWER_ID) != null) {
        return
    }

    val composeView =
        androidx.compose.ui.platform.ComposeView(activity).apply {
            id = VIEWER_ID
            // Ensures the ComposeView can handle touches properly
            isClickable = true
            isFocusable = true

            setContent {
                LogProvider(
                    uiConfig = UiConfig(showFloatingButton = false),
                    enabled = true,
                    content = {
                        val controller = LocalLogController.current

                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(PANEL_SHOW_DELAY_MS)
                            controller.show()

                            // Wait until the state actually reflects that it is visible
                            controller.isVisible.first { it }

                            // Now wait until it becomes invisible (e.g. user closed it)
                            controller.isVisible.first { !it }

                            // Remove the ComposeView from the Activity
                            decorView.post {
                                decorView.removeView(this@apply)
                            }
                        }

                        // Empty container - the panel will open over it
                        Box(modifier = Modifier.fillMaxSize())
                    },
                )
            }
        }

    // Add to the top of the DecorView with full screen bounds
    decorView.addView(
        composeView,
        android.view.ViewGroup.LayoutParams(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
        ),
    )
}

private val VIEWER_ID by lazy { View.generateViewId() }

/**
 * Delay before showing the panel overlay, in milliseconds.
 *
 * Ensures the ComposeView is fully measured and laid out before the
 * bottom-sheet animation begins, preventing a brief layout flicker.
 */
private const val PANEL_SHOW_DELAY_MS = 100L
