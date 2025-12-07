package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WindCompassMini(windDir: String?, modifier: Modifier = Modifier) {
    val degrees = windDirToDegrees(windDir)

    Canvas(modifier = modifier.fillMaxWidth().height(24.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(centerX, centerY) * 0.9f

        // Compass circle
        drawCircle(
            color = Color(0xFFE0E0E0),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )

        // Cardinal directions
        val textRadius = radius * 0.7f
        val directionColor = Color(0xFF9E9E9E)
        
        // N, E, S, W markers
        for (i in 0 until 4) {
            val angle = Math.toRadians((i * 90 - 90).toDouble())
            val markerRadius = radius * 0.85f
            drawLine(
                color = directionColor,
                start = Offset(centerX, centerY),
                end = Offset(
                    centerX + (markerRadius * cos(angle)).toFloat(),
                    centerY + (markerRadius * sin(angle)).toFloat()
                ),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Arrow pointing to wind direction
        if (degrees != null) {
            rotate(degrees.toFloat(), pivot = Offset(centerX, centerY)) {
                val arrowPath = Path().apply {
                    val arrowLength = radius * 0.6f
                    val arrowWidth = radius * 0.15f
                    
                    moveTo(centerX, centerY - arrowLength)
                    lineTo(centerX - arrowWidth, centerY)
                    lineTo(centerX, centerY - arrowLength * 0.7f)
                    lineTo(centerX + arrowWidth, centerY)
                    close()
                }
                
                drawPath(
                    path = arrowPath,
                    color = Color(0xFF2196F3)
                )
            }
        }
    }
}

private fun windDirToDegrees(dir: String?): Int? {
    return when (dir?.uppercase()) {
        "N" -> 0
        "NNE" -> 23
        "NE" -> 45
        "ENE" -> 68
        "E" -> 90
        "ESE" -> 113
        "SE" -> 135
        "SSE" -> 158
        "S" -> 180
        "SSW" -> 203
        "SW" -> 225
        "WSW" -> 248
        "W" -> 270
        "WNW" -> 293
        "NW" -> 315
        "NNW" -> 338
        else -> null
    }
}
