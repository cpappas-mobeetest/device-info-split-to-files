package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

@Composable
fun MiniMonthCalendar(millis: Long) {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = millis
    }

    val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = Modifier.size(deviceInfoIconSize28)) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Calendar background
        drawRect(
            color = Color(0xFFF5F5F5),
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, canvasHeight)
        )

        // Calendar border
        drawRect(
            color = Color(0xFFBDBDBD),
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, canvasHeight),
            style = Stroke(width = 1.dp.toPx())
        )

        // Header bar (red)
        val headerHeight = canvasHeight * 0.25f
        drawRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(0f, 0f),
            size = Size(canvasWidth, headerHeight)
        )

        // Day number
        val textStyle = TextStyle(
            fontSize = 12.sp,
            color = Color(0xFF212121)
        )
        val textLayoutResult = textMeasurer.measure(
            text = dayOfMonth.toString(),
            style = textStyle
        )

        val textX = (canvasWidth - textLayoutResult.size.width) / 2
        val textY = headerHeight + (canvasHeight - headerHeight - textLayoutResult.size.height) / 2

        drawText(
            textMeasurer = textMeasurer,
            text = dayOfMonth.toString(),
            topLeft = Offset(textX, textY),
            style = textStyle
        )
    }
}
