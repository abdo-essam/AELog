package com.ae.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A sleek vertical pill docked snug against the right edge of the screen.
 * Tapping it opens the AELog overlay panel.
 *
 * Rendered via a non-focusable Popup at Alignment.CenterEnd in AELogOverlay.
 */
@Composable
internal fun LogNotchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .width(32.dp)
                .height(120.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(Color(0xFF111111)) // Fully opaque deep charcoal
        // to prevent background colors from shining through
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClickLabel = "Open AELog panel",
                    onClick = onClick,
                ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Top: Bug icon
            Box(
                modifier =
                    Modifier
                        .padding(top = 10.dp)
                        .size(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Open AELog",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }

            // Center: Rotated text "AELOG"
            Box(
                modifier =
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "AELOG",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    modifier =
                        Modifier
                            .rotate(-90f)
                            .requiredWidth(76.dp), // Ignore parent constraints
            // to prevent "G" truncation
                    // Snug fit for vertical rotation
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }

            // Bottom spacing to balance the top padding
            Box(modifier = Modifier.height(10.dp))
        }
    }
}
