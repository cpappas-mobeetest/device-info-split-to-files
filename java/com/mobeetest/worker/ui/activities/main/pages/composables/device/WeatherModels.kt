package com.mobeetest.worker.ui.activities.main.pages.composables.device

import java.util.Locale

@Suppress("unused")
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

@Suppress("unused")
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

@Suppress("unused")
internal fun temperatureBucket(tempC: Double?): TemperatureBucket {
    val t = tempC ?: return TemperatureBucket.MILD
    return when {
        t <= 0.0 -> TemperatureBucket.FREEZING
        t <= 15.0 -> TemperatureBucket.COLD
        t <= 28.0 -> TemperatureBucket.MILD
        else -> TemperatureBucket.HOT
    }
}

@Suppress("unused")
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
