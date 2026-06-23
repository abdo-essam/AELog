package com.ae.log

import android.app.ActionBar
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import com.ae.log.ui.AELogOverlay
import kotlinx.coroutines.delay
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
            AELogOverlay()

            LaunchedEffect(Unit) {
                AELog.show()

                // Wait until the state actually reflects that it is visible
                AELog.instance?.overlayVisible?.first { it }

                // Now wait until it becomes invisible (e.g. user closed it)
                AELog.instance?.overlayVisible?.first { !it }

                // Finish the Activity
                finish()
            }
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
    if (decorView.findViewById<View>(VIEWER_ID) != null) {
        return
    }

    val composeView =
        ComposeView(activity).apply {
            id = VIEWER_ID
            // Ensures the ComposeView can handle touches properly
            isClickable = true
            isFocusable = true

            setContent {
                AELogOverlay()

                LaunchedEffect(Unit) {
                    delay(PANEL_SHOW_DELAY_MS)
                    AELog.show()

                    // Wait until the state actually reflects that it is visible
                    AELog.instance?.overlayVisible?.first { it }

                    // Now wait until it becomes invisible (e.g. user closed it)
                    AELog.instance?.overlayVisible?.first { !it }

                    // Remove the ComposeView from the Activity
                    decorView.post {
                        decorView.removeView(this@apply)
                    }
                }
            }
        }

    // Add to the top of the DecorView with full screen bounds
    decorView.addView(
        composeView,
        ActionBar.LayoutParams(
            ActionBar.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
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
