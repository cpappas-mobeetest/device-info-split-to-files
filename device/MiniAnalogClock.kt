package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MiniAnalogClock(timeMillis: Long) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = timeMillis
    }

    val hour = calendar.get(Calendar.HOUR)
    val minute = calendar.get(Calendar.MINUTE)
    val second = calendar.get(Calendar.SECOND)

    Canvas(modifier = Modifier.size(deviceInfoIconSize26)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val centerX = canvasWidth / 2
        val centerY = canvasHeight / 2
        val radius = minOf(centerX, centerY) * 0.9f

        // Clock circle
        drawCircle(
            color = Color(0xFFE0E0E0),
            radius = radius,
            center = Offset(centerX, centerY),
            style = Stroke(width = 1.dp.toPx())
        )

        // Hour marks
        for (i in 0 until 12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val startRadius = radius * 0.85f
            val endRadius = radius * 0.95f
            drawLine(
                color = Color(0xFFBDBDBD),
                start = Offset(
                    centerX + (startRadius * cos(angle)).toFloat(),
                    centerY + (startRadius * sin(angle)).toFloat()
                ),
                end = Offset(
                    centerX + (endRadius * cos(angle)).toFloat(),
                    centerY + (endRadius * sin(angle)).toFloat()
                ),
                strokeWidth = 1.dp.toPx()
            )
        }

        // Hour hand
        val hourAngle = Math.toRadians(((hour % 12) * 30 + minute * 0.5 - 90).toDouble())
        val hourHandLength = radius * 0.5f
        drawLine(
            color = Color(0xFF424242),
            start = Offset(centerX, centerY),
            end = Offset(
                centerX + (hourHandLength * cos(hourAngle)).toFloat(),
                centerY + (hourHandLength * sin(hourAngle)).toFloat()
            ),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute hand
        val minuteAngle = Math.toRadians((minute * 6 - 90).toDouble())
        val minuteHandLength = radius * 0.7f
        drawLine(
            color = Color(0xFF616161),
            start = Offset(centerX, centerY),
            end = Offset(
                centerX + (minuteHandLength * cos(minuteAngle)).toFloat(),
                centerY + (minuteHandLength * sin(minuteAngle)).toFloat()
            ),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Second hand
        val secondAngle = Math.toRadians((second * 6 - 90).toDouble())
        val secondHandLength = radius * 0.8f
        drawLine(
            color = Color(0xFFFF5252),
            start = Offset(centerX, centerY),
            end = Offset(
                centerX + (secondHandLength * cos(secondAngle)).toFloat(),
                centerY + (secondHandLength * sin(secondAngle)).toFloat()
            ),
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Center dot
        drawCircle(
            color = Color(0xFF424242),
            radius = 2.dp.toPx(),
            center = Offset(centerX, centerY)
        )
    }
}
