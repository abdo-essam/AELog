package com.ae.log.ui.layout.overlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.ae.log.ui.UiConfig
import com.ae.log.ui.theme.LogDimens

/**
 * Presents the AELog panel as a [ModalBottomSheet].
 * Suited for compact/phone screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
public object BottomSheetOverlay : OverlayStrategy {
    @Composable
    override fun Overlay(
        uiConfig: UiConfig,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape =
                RoundedCornerShape(
                    topStart = LogDimens.overlayCornerRadius,
                    topEnd = LogDimens.overlayCornerRadius,
                ),
        ) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(uiConfig.bottomSheetHeightFraction)
                        .nestedScroll(
                            remember {
                                object : NestedScrollConnection {
                                    override fun onPostScroll(
                                        consumed: Offset,
                                        available: Offset,
                                        source: NestedScrollSource,
                                    ): Offset = available

                                    override suspend fun onPostFling(
                                        consumed: Velocity,
                                        available: Velocity,
                                    ): Velocity = available
                                }
                            },
                        ),
            ) {
                content()
            }
        }
    }
}
