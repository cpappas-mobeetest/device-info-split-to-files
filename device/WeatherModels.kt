package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mobeetest.worker.R
import org.json.JSONObject
import java.util.Locale

internal data class WeatherParsedInfo(
    val city: String?,
    val region: String?,
    val country: String?,
    val localTime: String?,
    val temperatureC: Double?,
    val conditionText: String?,
    val isDay: Boolean?,
    val feelsLikeC: Double?,
    val windKph: Double?,
    val windDir: String?,
    val humidity: Int?,
    val uvIndex: Double?,
    val visibilityKm: Double?
)

internal enum class WeatherConditionCategory {
    CLEAR_DAY,
    CLEAR_NIGHT,
    PARTLY_CLOUDY,
    CLOUDY,
    FOG,
    RAIN,
    SNOW,
    STORM
}

internal enum class TemperatureBucket {
    FREEZING,
    COLD,
    MILD,
    HOT
}

internal enum class UvBucket {
    LOW,
    MODERATE,
    HIGH,
    VERY_HIGH,
    EXTREME
}

internal fun parseWeatherInfoJson(raw: String?): WeatherParsedInfo? {
    val jsonString = raw?.takeIf { it.isNotBlank() } ?: return null

    return try {
        val json = JSONObject(jsonString)
        val location = json.getJSONObject("location")
        val current = json.getJSONObject("current")
        val condition = current.getJSONObject("condition")

        val isDayRaw = current.optInt("is_day", -1)
        val isDay = when (isDayRaw) {
            0 -> false
            1 -> true
            else -> null
        }

        val city = location.optString("name")
            .takeIf { it.isNotBlank() }

        val region = location.optString("region")
            .takeIf { it.isNotBlank() }

        val country = location.optString("country")
            .takeIf { it.isNotBlank() }

        val localTime = location.optString("localtime")
            .takeIf { it.isNotBlank() }

        val tempRaw = current.optDouble("temp_c", Double.NaN)
        val temperatureC = tempRaw.takeUnless { it.isNaN() }

        val feelsRaw = current.optDouble("feelslike_c", Double.NaN)
        val feelsLikeC = feelsRaw.takeUnless { it.isNaN() }

        val windKphRaw = current.optDouble("wind_kph", Double.NaN)
        val windKph = windKphRaw.takeUnless { it.isNaN() }

        val windDir = current.optString("wind_dir")
            .takeIf { it.isNotBlank() }

        val humidityRaw = current.optInt("humidity", -1)
        val humidity = humidityRaw.takeUnless { it < 0 }

        val uvRaw = current.optDouble("uv", Double.NaN)
        val uvIndex = uvRaw.takeUnless { it.isNaN() }

        val visRaw = current.optDouble("vis_km", Double.NaN)
        val visibilityKm = visRaw.takeUnless { it.isNaN() }

        val conditionText = condition.optString("text")
            .takeIf { it.isNotBlank() }

        WeatherParsedInfo(
            city = city,
            region = region,
            country = country,
            localTime = localTime,
            temperatureC = temperatureC,
            conditionText = conditionText,
            isDay = isDay,
            feelsLikeC = feelsLikeC,
            windKph = windKph,
            windDir = windDir,
            humidity = humidity,
            uvIndex = uvIndex,
            visibilityKm = visibilityKm
        )
    } catch (_: Exception) {
        null
    }
}

internal fun categorizeWeatherCondition(
    text: String?,
    isDay: Boolean?
): WeatherConditionCategory {
    val t = text?.lowercase(Locale.US).orEmpty()

    return when {
        "thunder" in t || "storm" in t -> WeatherConditionCategory.STORM
        "snow" in t || "sleet" in t || "blizzard" in t || "ice pellet" in t ->
            WeatherConditionCategory.SNOW
        "rain" in t || "drizzle" in t || "shower" in t ->
            WeatherConditionCategory.RAIN
        "fog" in t || "mist" in t || "haze" in t ->
            WeatherConditionCategory.FOG
        "overcast" in t || "cloudy" in t ->
            WeatherConditionCategory.CLOUDY
        "partly" in t || "patchy" in t ->
            WeatherConditionCategory.PARTLY_CLOUDY
        "sunny" in t || "clear" in t -> {
            if (isDay == false) WeatherConditionCategory.CLEAR_NIGHT
            else WeatherConditionCategory.CLEAR_DAY
        }
        else -> {
            if (isDay == false) WeatherConditionCategory.CLEAR_NIGHT
            else WeatherConditionCategory.CLEAR_DAY
        }
    }
}

@DrawableRes
internal fun iconForCondition(
    text: String?,
    isDay: Boolean?
): Int {
    return when (categorizeWeatherCondition(text, isDay)) {
        WeatherConditionCategory.CLEAR_DAY -> R.drawable.weather_condition_clear_day
        WeatherConditionCategory.CLEAR_NIGHT -> R.drawable.weather_condition_clear_night
        WeatherConditionCategory.PARTLY_CLOUDY -> R.drawable.weather_condition_partly_cloudy
        WeatherConditionCategory.CLOUDY -> R.drawable.weather_condition_cloudy
        WeatherConditionCategory.FOG -> R.drawable.weather_condition_fog
        WeatherConditionCategory.RAIN -> R.drawable.weather_condition_rain
        WeatherConditionCategory.SNOW -> R.drawable.weather_condition_snow
        WeatherConditionCategory.STORM -> R.drawable.weather_condition_storm
    }
}

internal fun temperatureBucket(tempC: Double?): TemperatureBucket {
    val t = tempC ?: return TemperatureBucket.MILD
    return when {
        t <= 0.0 -> TemperatureBucket.FREEZING
        t <= 15.0 -> TemperatureBucket.COLD
        t <= 28.0 -> TemperatureBucket.MILD
        else -> TemperatureBucket.HOT
    }
}

@DrawableRes
internal fun iconForTemperature(tempC: Double?): Int {
    return when (temperatureBucket(tempC)) {
        TemperatureBucket.FREEZING -> R.drawable.weather_temp_freezing
        TemperatureBucket.COLD -> R.drawable.weather_temp_cold
        TemperatureBucket.MILD -> R.drawable.weather_temp_mild
        TemperatureBucket.HOT -> R.drawable.weather_temp_hot
    }
}

internal fun uvBucket(uv: Double?): UvBucket {
    val v = uv ?: return UvBucket.LOW
    return when {
        v < 3.0 -> UvBucket.LOW
        v < 6.0 -> UvBucket.MODERATE
        v < 8.0 -> UvBucket.HIGH
        v < 11.0 -> UvBucket.VERY_HIGH
        else -> UvBucket.EXTREME
    }
}

@DrawableRes
internal fun iconForUvIndex(uv: Double?): Int {
    return when (uvBucket(uv)) {
        UvBucket.LOW -> R.drawable.weather_uv_low
        UvBucket.MODERATE -> R.drawable.weather_uv_moderate
        UvBucket.HIGH -> R.drawable.weather_uv_high
        UvBucket.VERY_HIGH -> R.drawable.weather_uv_very_high
        UvBucket.EXTREME -> R.drawable.weather_uv_extreme
    }
}

internal fun temperatureColor(tempC: Double?): @Composable () -> Color = {
    when (temperatureBucket(tempC)) {
        TemperatureBucket.FREEZING -> MaterialTheme.colorScheme.secondary
        TemperatureBucket.COLD -> MaterialTheme.colorScheme.primary
        TemperatureBucket.MILD -> MaterialTheme.colorScheme.tertiary
        TemperatureBucket.HOT -> MaterialTheme.colorScheme.error
    }
}

internal fun windDirToDegrees(dir: String?): Float? {
    val d = dir?.trim()?.uppercase(Locale.US) ?: return null
    return when (d) {
        "N" -> 0f
        "NNE" -> 22.5f
        "NE" -> 45f
        "ENE" -> 67.5f
        "E" -> 90f
        "ESE" -> 112.5f
        "SE" -> 135f
        "SSE" -> 157.5f
        "S" -> 180f
        "SSW" -> 202.5f
        "SW" -> 225f
        "WSW" -> 247.5f
        "W" -> 270f
        "WNW" -> 292.5f
        "NW" -> 315f
        "NNW" -> 337.5f
        else -> null
    }
}
