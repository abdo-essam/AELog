package com.ae.log

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.ae.log.core.LocalLogController
import kotlinx.coroutines.flow.first

/**
 * A dedicated Activity to show the AELog interface in View-based Android apps.
 *
 * Launch this from anywhere in your app:
 * ```kotlin
 * startActivity(Intent(context, AELogViewerActivity::class.java))
 * ```
 */
public class AELogViewerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This makes sure the background is somewhat transparent or just black
        window.setBackgroundDrawableResource(android.R.color.transparent)

        setContent {
            AELogProvider(
                uiConfig = AELogUiConfig(showFloatingButton = false),
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
public fun AELog.Companion.launchViewer(context: android.content.Context) {
    val activity = context as? android.app.Activity ?: return
    val decorView = activity.window.decorView as android.view.ViewGroup

    // Check if we already added the viewer to avoid duplicates
    val viewId = 999999
    if (decorView.findViewById<android.view.View>(viewId) != null) {
        return
    }

    val composeView =
        androidx.compose.ui.platform.ComposeView(activity).apply {
            id = viewId
            // Ensures the ComposeView can handle touches properly
            isClickable = true
            isFocusable = true

            setContent {
                AELogProvider(
                    uiConfig = AELogUiConfig(showFloatingButton = false),
                    enabled = true,
                    content = {
                        val controller = LocalLogController.current

                        LaunchedEffect(Unit) {
                            // Small delay to ensure the layout is fully measured before animating the bottom sheet
                            kotlinx.coroutines.delay(100)
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
