package com.mobeetest.worker.ui.activities.main.pages.composables.device

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatDate(millis: Long): String {
    if (millis <= 0L) return "Unknown"
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
}

internal fun formatDateTime(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(millis))
}


@Suppress("unused")
internal fun formatDateTimeNoSeconds(millis: Long): String {
    if (millis <= 0L) return "Unknown"
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(millis))
}
