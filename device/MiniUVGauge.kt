package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun MiniUVGauge(uvIndex: Double?, modifier: Modifier = Modifier) {
    val uv = uvIndex ?: 0.0
    val normalizedUv = (uv / 11.0).coerceIn(0.0, 1.0)

    Canvas(modifier = modifier.size(deviceInfoIconSize24)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = min(canvasWidth, canvasHeight) / 2 * 0.8f

        // Background arc
        drawArc(
            color = Color(0xFFE0E0E0),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(canvasWidth / 2 - radius, canvasHeight / 2 - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )

        // UV level arc
        val uvColor = when {
            uv < 3.0 -> Color(0xFF4CAF50)  // Low - green
            uv < 6.0 -> Color(0xFFFDD835)  // Moderate - yellow
            uv < 8.0 -> Color(0xFFFF9800)  // High - orange
            uv < 11.0 -> Color(0xFFE53935)  // Very high - red
            else -> Color(0xFF6A1B9A)  // Extreme - purple
        }

        drawArc(
            color = uvColor,
            startAngle = 180f,
            sweepAngle = (180f * normalizedUv).toFloat(),
            useCenter = false,
            topLeft = Offset(canvasWidth / 2 - radius, canvasHeight / 2 - radius),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
