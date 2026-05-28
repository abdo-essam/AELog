package com.ae.log.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogSpacing

/**
 * A compact Dynamic Island-style pill that sits just below the status bar.
 * Tapping it opens the AELog overlay panel.
 *
 * Rendered via a non-focusable [Popup] in [com.ae.log.ui.AELogOverlay],
 * so it overlays app content without any wrapping required.
 */
@Composable
internal fun LogNotchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pillShape = RoundedCornerShape(percent = 50)

    // Subtle ambient glow pulse on the border
    val pulse by rememberInfiniteTransition(label = "notch_pulse").animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "notch_alpha",
    )

    Box(
        modifier = modifier
            .statusBarsPadding()
            .padding(top = LogSpacing.x1),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(min = 56.dp)
                .shadow(elevation = 6.dp, shape = pillShape)
                .clip(pillShape)
                .background(Color(0xF0000000))
                .drawBehind {
                    // Animated ring border
                    drawRoundRect(
                        color = Color.White.copy(alpha = pulse),
                        style = Stroke(width = 1.dp.toPx()),
                        cornerRadius =
                            androidx.compose.ui.geometry.CornerRadius(size.height / 2),
                    )
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = "Open AELog panel",
                ) { onClick() }
                .padding(horizontal = LogSpacing.x3, vertical = LogSpacing.x1_5),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "Open AELog",
                tint = Color.White,
                modifier = Modifier.size(LogSpacing.x4),
            )
        }
    }
}
