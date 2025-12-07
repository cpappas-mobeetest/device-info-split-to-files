package com.mobeetest.worker.ui.activities.main.pages.composables.device

import org.json.JSONObject

/**
 * Data class representing weather information
 */
data class WeatherInfo(
    val location: LocationInfo?,
    val current: CurrentWeatherInfo?
)

data class LocationInfo(
    val name: String?,
    val region: String?,
    val country: String?,
    val lat: Double?,
    val lon: Double?,
    val localtime: String?
)

data class CurrentWeatherInfo(
    val lastUpdatedEpoch: Long?,
    val lastUpdated: String?,
    val tempC: Double?,
    val tempF: Double?,
    val feelslikeC: Double?,
    val feelslikeF: Double?,
    val condition: ConditionInfo?,
    val windKph: Double?,
    val windMph: Double?,
    val windDegree: Int?,
    val windDir: String?,
    val pressureMb: Double?,
    val pressureIn: Double?,
    val precipMm: Double?,
    val precipIn: Double?,
    val humidity: Int?,
    val cloud: Int?,
    val uv: Double?,
    val visKm: Double?,
    val visMiles: Double?
)

data class ConditionInfo(
    val text: String?,
    val icon: String?,
    val code: Int?
)

/**
 * Parse weather JSON string into WeatherInfo object
 */
internal fun parseWeatherInfo(jsonString: String?): WeatherInfo? {
    if (jsonString.isNullOrBlank()) return null

    return try {
        val json = JSONObject(jsonString)
        
        val locationJson = json.optJSONObject("location")
        val currentJson = json.optJSONObject("current")
        
        val location = locationJson?.let {
            LocationInfo(
                name = it.optString("name").takeIf { s -> s.isNotBlank() },
                region = it.optString("region").takeIf { s -> s.isNotBlank() },
                country = it.optString("country").takeIf { s -> s.isNotBlank() },
                lat = it.optDouble("lat").takeUnless { d -> d.isNaN() },
                lon = it.optDouble("lon").takeUnless { d -> d.isNaN() },
                localtime = it.optString("localtime").takeIf { s -> s.isNotBlank() }
            )
        }
        
        val current = currentJson?.let {
            val conditionJson = it.optJSONObject("condition")
            val condition = conditionJson?.let { cond ->
                ConditionInfo(
                    text = cond.optString("text").takeIf { s -> s.isNotBlank() },
                    icon = cond.optString("icon").takeIf { s -> s.isNotBlank() },
                    code = cond.optInt("code", -1).takeUnless { c -> c == -1 }
                )
            }
            
            CurrentWeatherInfo(
                lastUpdatedEpoch = it.optLong("last_updated_epoch", -1L).takeUnless { e -> e == -1L },
                lastUpdated = it.optString("last_updated").takeIf { s -> s.isNotBlank() },
                tempC = it.optDouble("temp_c").takeUnless { d -> d.isNaN() },
                tempF = it.optDouble("temp_f").takeUnless { d -> d.isNaN() },
                feelslikeC = it.optDouble("feelslike_c").takeUnless { d -> d.isNaN() },
                feelslikeF = it.optDouble("feelslike_f").takeUnless { d -> d.isNaN() },
                condition = condition,
                windKph = it.optDouble("wind_kph").takeUnless { d -> d.isNaN() },
                windMph = it.optDouble("wind_mph").takeUnless { d -> d.isNaN() },
                windDegree = it.optInt("wind_degree", -1).takeUnless { deg -> deg == -1 },
                windDir = it.optString("wind_dir").takeIf { s -> s.isNotBlank() },
                pressureMb = it.optDouble("pressure_mb").takeUnless { d -> d.isNaN() },
                pressureIn = it.optDouble("pressure_in").takeUnless { d -> d.isNaN() },
                precipMm = it.optDouble("precip_mm").takeUnless { d -> d.isNaN() },
                precipIn = it.optDouble("precip_in").takeUnless { d -> d.isNaN() },
                humidity = it.optInt("humidity", -1).takeUnless { h -> h == -1 },
                cloud = it.optInt("cloud", -1).takeUnless { c -> c == -1 },
                uv = it.optDouble("uv").takeUnless { d -> d.isNaN() },
                visKm = it.optDouble("vis_km").takeUnless { d -> d.isNaN() },
                visMiles = it.optDouble("vis_miles").takeUnless { d -> d.isNaN() }
            )
        }
        
        WeatherInfo(location = location, current = current)
    } catch (e: Exception) {
        null
    }
}

/**
 * Parse weather time from epoch or string
 */
internal fun parseWeatherTime(epoch: Long?, timeString: String?): Long? {
    if (epoch != null && epoch > 0) {
        return epoch * 1000 // Convert to milliseconds
    }
    
    if (timeString != null) {
        return try {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .parse(timeString)?.time
        } catch (e: Exception) {
            null
        }
    }
    
    return null
}
