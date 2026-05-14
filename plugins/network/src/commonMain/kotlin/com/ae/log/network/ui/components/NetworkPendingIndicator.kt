package com.ae.log.network.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ae.log.ui.theme.LogSpacing

/** Animated three-dot indicator shown while a network response is still in-flight. */
@Composable
internal fun NetworkPendingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "waiting_dots")

    @Composable
    fun dot(delayMs: Int): Float {
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(500, delayMillis = delayMs, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "dot_$delayMs",
        )
        return alpha
    }

    val alphas = listOf(dot(0), dot(160), dot(320))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LogSpacing.x1_5),
    ) {
        alphas.forEach { alpha ->
            Box(
                modifier =
                    Modifier
                        .size(7.dp)
                        .alpha(alpha)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
            )
        }
        Spacer(Modifier.width(LogSpacing.x1))
        Text(
            text = "Waiting for response\u2026",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
