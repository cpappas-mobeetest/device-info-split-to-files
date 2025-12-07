package com.mobeetest.worker.ui.activities.main.pages.composables.device

import org.json.JSONObject
import java.util.Locale

internal fun boolYesNo(value: Boolean): String = if (value) "Yes" else "No"

internal fun formatMegabytes(valueMb: Float): String {
    return if (valueMb >= 1024f) {
        val gb = valueMb / 1024f
        String.format(Locale.US, "%.1f GB", gb)
    } else {
        String.format(Locale.US, "%.0f MB", valueMb)
    }
}

internal fun formatBytes(valueBytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    val tb = gb * 1024

    val v = valueBytes.toDouble()
    return when {
        v >= tb -> String.format(Locale.US, "%.2f TB", v / tb)
        v >= gb -> String.format(Locale.US, "%.2f GB", v / gb)
        v >= mb -> String.format(Locale.US, "%.1f MB", v / mb)
        v >= kb -> String.format(Locale.US, "%.0f KB", v / kb)
        else -> "$valueBytes B"
    }
}

internal fun formatWeatherInfo(raw: String): Map<String, String> {
    return try {
        val weatherJson = JSONObject(raw)
        val location = weatherJson.getJSONObject("location")
        val current = weatherJson.getJSONObject("current")
        val condition = current.getJSONObject("condition")

        mapOf(
            "City" to location.optString("name"),
            "Region" to location.optString("region"),
            "Country" to location.optString("country"),
            "Local time" to location.optString("localtime"),
            "Temperature" to "${current.optDouble("temp_c")} °C",
            "Condition" to condition.optString("text"),
            "Feels like" to "${current.optDouble("feelslike_c")} °C",
            "Wind" to "${current.optDouble("wind_kph")} kph (${current.optString("wind_dir")})",
            "Humidity" to "${current.optInt("humidity")} %",
            "UV index" to current.optDouble("uv").toString(),
            "Visibility" to "${current.optDouble("vis_km")} km"
        )
    } catch (e: Exception) {
        mapOf("Weather info" to "Invalid JSON: ${e.localizedMessage}")
    }
}

// Overloaded function for dual-unit weather values (metric + imperial)
internal fun formatWeatherInfo(
    metricValue: Double?,
    imperialValue: Double?,
    metricUnit: String,
    imperialUnit: String
): String {
    return if (metricValue != null && imperialValue != null) {
        String.format(
            Locale.US,
            "%.1f %s / %.1f %s",
            metricValue,
            metricUnit,
            imperialValue,
            imperialUnit
        )
    } else {
        "N/A"
    }
}
