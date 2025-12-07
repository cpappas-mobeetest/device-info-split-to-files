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
import kotlin.math.max
import kotlin.math.min

@Composable
fun ThermometerMini(temperatureC: Double, modifier: Modifier = Modifier) {
    val tempColor = temperatureColor(temperatureC)
    val normalizedTemp = ((temperatureC + 10.0) / 50.0).coerceIn(0.0, 1.0)

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

        // Filled bar
        val fillWidth = (canvasWidth * normalizedTemp).toFloat()
        if (fillWidth > 0) {
            drawRoundRect(
                color = tempColor,
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

private fun temperatureColor(tempC: Double): Color {
    return when {
        tempC <= 0.0 -> Color(0xFF1565C0)  // Freezing - dark blue
        tempC <= 15.0 -> Color(0xFF42A5F5)  // Cold - light blue
        tempC <= 28.0 -> Color(0xFF66BB6A)  // Mild - green
        else -> Color(0xFFEF5350)  // Hot - red
    }
}
