package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun MiniVisibilityBar(visibilityKm: Double?, modifier: Modifier = Modifier) {
    val vis = visibilityKm ?: 0.0
    val normalizedVis = (vis / 10.0).coerceIn(0.0, 1.0)

    Canvas(modifier = modifier.fillMaxWidth().height(20.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barHeight = canvasHeight * 0.6f
        val barTop = (canvasHeight - barHeight) / 2

        // Background bar
        drawRoundRect(
            color = Color(0xFFE0E0E0),
            topLeft = Offset(0f, barTop),
            size = Size(canvasWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
        )

        // Filled bar (visibility level)
        val fillWidth = (canvasWidth * normalizedVis).toFloat()
        val visColor = when {
            vis < 1.0 -> Color(0xFFB71C1C)  // Very poor - dark red
            vis < 4.0 -> Color(0xFFFF9800)  // Poor - orange
            vis < 10.0 -> Color(0xFF66BB6A)  // Moderate - green
            else -> Color(0xFF2196F3)  // Good - blue
        }

        if (fillWidth > 0) {
            drawRoundRect(
                color = visColor,
                topLeft = Offset(0f, barTop),
                size = Size(fillWidth, barHeight),
                cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)
            )
        }

        // Border
        drawRoundRect(
            color = Color(0xFFBDBDBD),
            topLeft = Offset(0f, barTop),
            size = Size(canvasWidth, barHeight),
            cornerRadius = CornerRadius(barHeight / 2, barHeight / 2),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
