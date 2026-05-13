package com.ae.log.plugins.network.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.ae.log.plugins.network.model.NetworkEntry
import com.ae.log.plugins.network.ui.theme.NetworkColors

@Composable
internal fun MethodBadge(label: String) {
    val color =
        NetworkColors
            .getMethodColor(label)
    Box(
        modifier =
            Modifier
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
internal fun StatusBadge(entry: NetworkEntry) {
    if (entry.isPending) {
        // Pulsing dot + label while waiting for a response
        val infiniteTransition = rememberInfiniteTransition(label = "pending_pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec =
                infiniteRepeatable(
                    animation = tween(600, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse,
                ),
            label = "pending_alpha",
        )
        androidx.compose.foundation.layout.Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement =
                androidx.compose.foundation.layout.Arrangement
                    .spacedBy(4.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(6.dp)
                        .alpha(alpha)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            CircleShape,
                        ),
            )
            Text(
                text = "Waiting…",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
            )
        }
    } else {
        val text = entry.statusLabel
        val color =
            when {
                entry.isError -> MaterialTheme.colorScheme.error
                else -> NetworkColors.getStatusCodeColor(entry.statusCode)
            }
        Text(text = text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}
