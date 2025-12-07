package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp
) {
    val colorScheme = MaterialTheme.colorScheme
    val trackColor = colorScheme.onSurface.copy(alpha = 0.08f)
    val thumbColor = colorScheme.primary.copy(alpha = 0.9f)

    Canvas(
        modifier = modifier
            .fillMaxHeight()
            .width(thickness)
    ) {
        // Track
        drawRoundRect(
            color = trackColor,
            cornerRadius = CornerRadius(
                x = size.width / 2f,
                y = size.width / 2f
            )
        )

        if (scrollState.maxValue > 0) {
            // Content & visible fraction
            val contentHeight = scrollState.maxValue.toFloat() + size.height
            val visibleFraction = size.height / contentHeight

            // Dynamic thumb height based on how much content fits on screen
            val thumbHeight = (size.height * visibleFraction)
                .coerceAtLeast(size.height * 0.05f) // optional minimum 5%

            val maxOffset = size.height - thumbHeight

            val fraction = (scrollState.value.toFloat() / scrollState.maxValue.toFloat())
                .coerceIn(0f, 1f)

            val thumbOffsetY = maxOffset * fraction

            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(0f, thumbOffsetY),
                size = Size(size.width, thumbHeight),
                cornerRadius = CornerRadius(
                    x = size.width / 2f,
                    y = size.width / 2f
                )
            )
        }
    }
}
