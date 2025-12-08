package com.mobeetest.worker.ui.activities.main.pages.composables.device

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import java.util.*
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.foundation.Canvas

@Suppress("unused")
@Composable
fun MiniAnalogClock(
    millis: Long,
    modifier: Modifier = Modifier
) {
    if (millis <= 0L) {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Unknown time", style = MaterialTheme.typography.labelSmall)
        }
        return
    }

    val cal = remember(millis) {
        Calendar.getInstance().apply { timeInMillis = millis }
    }
    val hour = cal.get(Calendar.HOUR)
    val minute = cal.get(Calendar.MINUTE)
    val second = cal.get(Calendar.SECOND)

    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val background = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
    val handColor = MaterialTheme.colorScheme.primary
    val secondHandColor = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
    val tickColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

    Canvas(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, outline, RoundedCornerShape(12.dp))
            .background(background)
            .padding(10.dp)
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Draw clock circle
        drawCircle(
            color = outline,
            radius = radius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw hour markers and numbers
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = radius * 0.25f
            color = labelColor.toArgb()
            typeface = Typeface.DEFAULT
            textAlign = Paint.Align.CENTER
        }

        for (i in 1..12) {
            val angle = Math.toRadians((i * 30 - 90).toDouble())
            val isMainHour = i % 3 == 0

            if (isMainHour) {
                // Draw number
                val textRadius = radius * 0.75f
                val x = center.x + (cos(angle) * textRadius).toFloat()
                val y = center.y + (sin(angle) * textRadius).toFloat()
                val fm = paint.fontMetrics
                val baselineY = y - (fm.ascent + fm.descent) / 2f

                drawContext.canvas.nativeCanvas.drawText(
                    i.toString(),
                    x,
                    baselineY,
                    paint
                )
            } else {
                // Draw small tick
                val tickStart = radius * 0.85f
                val tickEnd = radius * 0.92f
                val startX = center.x + (cos(angle) * tickStart).toFloat()
                val startY = center.y + (sin(angle) * tickStart).toFloat()
                val endX = center.x + (cos(angle) * tickEnd).toFloat()
                val endY = center.y + (sin(angle) * tickEnd).toFloat()

                drawLine(
                    color = tickColor,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }

        // Draw hour hand
        val hourAngle = Math.toRadians(((hour % 12) * 30 + minute * 0.5 - 90))
        val hourLength = radius * 0.5f
        val hourEnd = Offset(
            x = center.x + (cos(hourAngle) * hourLength).toFloat(),
            y = center.y + (sin(hourAngle) * hourLength).toFloat()
        )
        drawLine(
            color = handColor,
            start = center,
            end = hourEnd,
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw minute hand
        val minuteAngle = Math.toRadians((minute * 6 - 90).toDouble())
        val minuteLength = radius * 0.7f
        val minuteEnd = Offset(
            x = center.x + (cos(minuteAngle) * minuteLength).toFloat(),
            y = center.y + (sin(minuteAngle) * minuteLength).toFloat()
        )
        drawLine(
            color = handColor,
            start = center,
            end = minuteEnd,
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw second hand (thin, red/error color)
        val secondAngle = Math.toRadians((second * 6 - 90).toDouble())
        val secondLength = radius * 0.8f
        val secondEnd = Offset(
            x = center.x + (cos(secondAngle) * secondLength).toFloat(),
            y = center.y + (sin(secondAngle) * secondLength).toFloat()
        )
        drawLine(
            color = secondHandColor,
            start = center,
            end = secondEnd,
            strokeWidth = 1.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Draw center dot
        drawCircle(
            color = handColor,
            radius = 4.dp.toPx(),
            center = center
        )
    }
}
