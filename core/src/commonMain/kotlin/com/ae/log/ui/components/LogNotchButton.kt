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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.ui.theme.LogTheme
import com.ae.log.ui.theme.LogThemeMode

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
    themeMode: LogThemeMode = LogThemeMode.SYSTEM,
) {
    LogTheme(themeMode = themeMode) {
        Box(
            modifier =
                modifier
                    .width(32.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(LogTheme.colors.primary)
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
                        tint = LogTheme.colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }

                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "AELOG",
                        color = LogTheme.colors.onPrimary.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.rotate(-90f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                Box(modifier = Modifier.height(10.dp))
            }
        }
    }
}
