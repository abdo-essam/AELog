package com.ae.log.ui.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.ae.log.ui.UiConfig

/**
 * Presents the AELog panel as a [ModalBottomSheet].
 * Suited for compact/phone screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
public object BottomSheetStrategy : PresentationStrategy {
    @Composable
    override fun Present(
        uiConfig: UiConfig,
        onDismiss: () -> Unit,
        content: @Composable () -> Unit,
    ) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            Box(
                modifier =
                    androidx.compose.ui.Modifier
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
