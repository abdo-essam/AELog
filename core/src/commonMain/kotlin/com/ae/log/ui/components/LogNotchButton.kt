package com.ae.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ae.log.ui.theme.LogTheme
import com.ae.log.ui.theme.LogThemeMode

/**
 * A sleek vertical pill notch floating view that can be dragged and positioned anywhere on screen.
 * Tapping it opens the AELog overlay panel.
 *
 * Rendered via a non-focusable Popup at an IntOffset position in AELogOverlay.
 */
@Composable
internal fun LogNotchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onDrag: ((Offset) -> Unit)? = null,
    onDragEnd: (() -> Unit)? = null,
    themeMode: LogThemeMode = LogThemeMode.SYSTEM,
) {
    LogTheme(themeMode = themeMode) {
        val shape = RoundedCornerShape(16.dp)
        Box(
            modifier =
                modifier
                    .shadow(8.dp, shape)
                    .width(36.dp)
                    .height(120.dp)
                    .clip(shape)
                    .background(LogTheme.colors.primary)
                    .then(
                        if ((onDrag != null) && (onDragEnd != null)) {
                            Modifier.floatingNotchGestures(
                                onClick = onClick,
                                onDrag = onDrag,
                                onDragEnd = onDragEnd,
                            )
                        } else {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClickLabel = "Open AELog panel",
                                onClick = onClick,
                            )
                        },
                    ),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top drag grip handle
                Box(
                    modifier =
                        Modifier
                            .padding(top = 6.dp)
                            .width(12.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(LogTheme.colors.onPrimary.copy(alpha = 0.5f)),
                )

                Box(
                    modifier =
                        Modifier
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
                        modifier =
                            Modifier
                                .requiredWidth(IntrinsicSize.Max)
                                .rotate(-90f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        softWrap = false,
                    )
                }

                // Bottom drag grip handle
                Box(
                    modifier =
                        Modifier
                            .padding(bottom = 6.dp)
                            .width(12.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(LogTheme.colors.onPrimary.copy(alpha = 0.5f)),
                )
            }
        }
    }
}

private fun Modifier.floatingNotchGestures(
    onClick: () -> Unit,
    onDrag: (Offset) -> Unit,
    onDragEnd: () -> Unit,
): Modifier =
    this.pointerInput(onClick, onDrag, onDragEnd) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            var isDrag = false
            var accumulated = Offset.Zero
            val touchSlop = viewConfiguration.touchSlop

            while (true) {
                val event = awaitPointerEvent()
                val dragEvent = event.changes.firstOrNull { it.id == down.id }
                if ((dragEvent == null) || (!dragEvent.pressed)) {
                    if (isDrag) onDragEnd() else onClick()
                    return@awaitEachGesture
                }

                val change = dragEvent.positionChange()
                if (change == Offset.Zero) continue

                accumulated += change
                if (accumulated.getDistance() > touchSlop) {
                    isDrag = true
                }

                if (isDrag) {
                    dragEvent.consume()
                    onDrag(change)
                }
            }
        }
    }
