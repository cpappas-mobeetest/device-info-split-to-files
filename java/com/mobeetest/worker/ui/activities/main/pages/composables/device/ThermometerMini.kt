package com.mobeetest.worker.ui.activities.main.pages.composables.device

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import java.util.Locale

@Composable
fun ThermometerMini(
    tempC: Double,
    modifier: Modifier = Modifier,
    minC: Float = -10f,
    maxC: Float = 40f
) {
    val clamped = tempC.toFloat().coerceIn(minC, maxC)
    val fraction = ((clamped - minC) / (maxC - minC)).coerceIn(0f, 1f)

    val outline = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    val mercury = temperatureColor(tempC).invoke()
    val tick = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val currentTempColor = Color(0xFF1565C0) // Blue
    val scaleLabelColor = Color.Black

    val labelText = String.format(Locale.US, "%.1f°C", tempC)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Geometry
        val bulbRadius = w * 0.28f
        val tubeWidth = w * 0.28f
        val tubeLeft = (w - tubeWidth) / 2f
        val tubeRight = tubeLeft + tubeWidth
        val tubeTop = h * 0.08f
        val tubeBottom = h - bulbRadius * 2.1f
        val tubeHeight = (tubeBottom - tubeTop).coerceAtLeast(1f)

        // Outline tube
        drawRoundRect(
            color = outline,
            topLeft = Offset(tubeLeft, tubeTop),
            size = Size(tubeWidth, tubeHeight),
            cornerRadius = CornerRadius(tubeWidth / 2f, tubeWidth / 2f),
            style = Stroke(width = w * 0.06f)
        )

        // Bulb outline
        val bulbCenter = Offset(w / 2f, h - bulbRadius)
        drawCircle(
            color = outline,
            radius = bulbRadius,
            center = bulbCenter,
            style = Stroke(width = w * 0.06f)
        )

        // Ticks (5)
        val tickCount = 5
        for (i in 0 until tickCount) {
            val y = tubeTop + (tubeHeight * i / (tickCount - 1).toFloat())
            drawLine(
                color = tick,
                start = Offset(tubeRight + w * 0.06f, y),
                end = Offset(tubeRight + w * 0.22f, y),
                strokeWidth = w * 0.03f
            )
        }

        // Mercury column
        val mercuryInset = w * 0.06f
        val innerLeft = tubeLeft + mercuryInset
        val innerWidth = tubeWidth - 2 * mercuryInset
        val mercuryHeight = tubeHeight * fraction
        val mercuryTop = tubeTop + tubeHeight - mercuryHeight

        drawRoundRect(
            color = mercury,
            topLeft = Offset(innerLeft, mercuryTop),
            size = Size(innerWidth, mercuryHeight),
            cornerRadius = CornerRadius(innerWidth / 2f, innerWidth / 2f)
        )

        // Mercury bulb fill
        drawCircle(
            color = mercury,
            radius = bulbRadius * 0.78f,
            center = bulbCenter
        )

        // Current temperature label (LEFT side, blue, bold, LARGER)
        val currentPaint = Paint().apply {
            isAntiAlias = true
            textSize = (w * 0.40f).coerceAtLeast(14f)
            color = currentTempColor.toArgb()
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        val currentTextX = tubeLeft - w * 0.08f
        val currentFm = currentPaint.fontMetrics
        val currentBaselineY = (mercuryTop - (currentFm.ascent + currentFm.descent) / 2f)
            .coerceIn(currentPaint.textSize, h - currentPaint.textSize)

        drawContext.canvas.nativeCanvas.drawText(
            labelText,
            currentTextX,
            currentBaselineY,
            currentPaint
        )

        // Scale labels (RIGHT side, black, normal)
        val scaleTemps = listOf(-10f, 0f, 20f, 40f)
        val scalePaint = Paint().apply {
            isAntiAlias = true
            textSize = (w * 0.24f).coerceAtLeast(9f)
            color = scaleLabelColor.toArgb()
            typeface = Typeface.DEFAULT
        }

        scaleTemps.forEach { scaleTemp ->
            // Skip if too close to current temperature
            if (kotlin.math.abs(scaleTemp - clamped) < 3f) return@forEach

            if (scaleTemp in minC..maxC) {
                val scaleFraction = ((scaleTemp - minC) / (maxC - minC)).coerceIn(0f, 1f)
                val scaleY = tubeTop + tubeHeight - (tubeHeight * scaleFraction)

                val scaleText = "${scaleTemp.toInt()}°C"
                val scaleX = tubeRight + w * 0.35f
                val fm = scalePaint.fontMetrics
                val baselineY = (scaleY - (fm.ascent + fm.descent) / 2f)
                    .coerceIn(scalePaint.textSize, h - scalePaint.textSize)

                drawContext.canvas.nativeCanvas.drawText(
                    scaleText,
                    scaleX,
                    baselineY,
                    scalePaint
                )
            }
        }
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
