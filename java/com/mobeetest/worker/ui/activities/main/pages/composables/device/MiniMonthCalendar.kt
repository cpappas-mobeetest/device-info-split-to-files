package com.mobeetest.worker.ui.activities.main.pages.composables.device

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Suppress("unused")
@Composable
fun MiniMonthCalendar(
    millis: Long,
    modifier: Modifier = Modifier
) {
    if (millis <= 0L) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                .padding(10.dp)
        ) {
            Text("Unknown date", style = MaterialTheme.typography.labelSmall)
        }
        return
    }

    val baseCal = remember(millis) {
        Calendar.getInstance().apply { timeInMillis = millis }
    }
    val monthCal = remember(millis) {
        (baseCal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val selectedDay = baseCal.get(Calendar.DAY_OF_MONTH)
    val daysInMonth = monthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

    val firstDow = monthCal.get(Calendar.DAY_OF_WEEK)
    val weekStart = monthCal.firstDayOfWeek

    fun dowIndex(dow: Int, start: Int): Int {
        var idx = dow - start
        if (idx < 0) idx += 7
        return idx
    }

    val offset = dowIndex(firstDow, weekStart)

    val cells = remember(millis) {
        MutableList<Int?>(42) { null }.also { list ->
            var day = 1
            var i = offset
            while (day <= daysInMonth && i < list.size) {
                list[i] = day
                day++
                i++
            }
        }
    }

    val monthTitle = remember(millis) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(monthCal.timeInMillis))
    }

    val dayNames = remember(weekStart) {
        val tmp = Calendar.getInstance()
        val names = mutableListOf<String>()
        for (i in 0..6) {
            val dow = ((weekStart - 1 + i) % 7) + 1
            tmp.set(Calendar.DAY_OF_WEEK, dow)
            names += SimpleDateFormat("EE", Locale.getDefault()).format(tmp.time)
        }
        names
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(12.dp)
            )
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
            .padding(10.dp)
    ) {
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(6.dp))

        Row(Modifier.fillMaxWidth()) {
            dayNames.forEach { name ->
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        for (row in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = cells[row * 7 + col]
                    val isSelected = day != null && day == selectedDay

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                else
                                    Color.Transparent
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                else
                                    Color.Transparent,
                                shape = RoundedCornerShape(6.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day?.toString() ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
