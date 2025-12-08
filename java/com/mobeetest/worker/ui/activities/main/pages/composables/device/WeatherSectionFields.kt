package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R

@Composable
fun WeatherSectionFields(weatherJson: String?, iconRes: Int) {
    val weather = remember(weatherJson) { parseWeatherInfo(weatherJson) }
    val parsed = remember(weather) { parseWeatherParsedInfo(weather) }
    val map = remember(weatherJson) { weatherJson?.let { formatWeatherInfo(it) } }
    
    var index = 1

    // If weather parsing failed, show unavailable message
    if (parsed == null || map == null || map.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = "Weather",
            value = "Not available",
            infoDescription = "The app could not retrieve weather information for this device location (location permission or network may be disabled).",
            showBottomDivider = false
        )
        return
    }

    // 1. Location
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_location),
        value = "${parsed.city ?: stringResource(R.string.device_info_unknown)}, ${parsed.country ?: ""}",
        infoDescription = "Location name and country from weather data provider."
    )

    // 2. Region
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_region),
        value = parsed.region ?: stringResource(R.string.device_info_unknown),
        infoDescription = "Administrative region (state / prefecture) for the location."
    )

    // 3. Coordinates
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_coordinates),
        value = "${weather?.location?.lat ?: "?"}, ${weather?.location?.lon ?: "?"}",
        infoDescription = "Geographic coordinates (latitude, longitude) of the location."
    )

    // 4. Local time
    val localTimeMillis = parsed.localTime?.let { timeStr ->
        try {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                .parse(timeStr)?.time
        } catch (_: Exception) {
            null
        }
    }

    if (localTimeMillis != null) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_local_time),
            value = formatDateTimeNoSeconds(localTimeMillis),
            infoDescription = "Local date and time in the reported city."
        )
    } else {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_local_time),
            value = map["Local time"] ?: stringResource(R.string.device_info_unknown),
            infoDescription = "Local date and time in the reported city."
        )
    }

    // 5. Condition
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_condition),
        value = parsed.conditionText ?: stringResource(R.string.device_info_unknown),
        infoDescription = "Current weather condition description."
    )

    // 6-9: Use WeatherAlignedFieldsGroup for Temperature, Feels like, Wind, Humidity
    WeatherAlignedFieldsGroup(
        startIndex = index,
        temperatureC = parsed.temperatureC,
        feelsLikeC = parsed.feelsLikeC,
        windKph = parsed.windKph,
        windDir = parsed.windDir,
        humidity = parsed.humidity,
        temperatureIconRes = iconRes,
        feelsLikeIconRes = iconRes,
        windIconRes = iconRes,
        humidityIconRes = iconRes
    )
    index += 4

    // 10. Pressure
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_pressure),
        value = formatWeatherInfo(weather?.current?.pressureMb, weather?.current?.pressureIn, "mb", "in"),
        infoDescription = "Atmospheric pressure in millibars and inches of mercury."
    )

    // 11. Precipitation
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_precipitation),
        value = formatWeatherInfo(weather?.current?.precipMm, weather?.current?.precipIn, "mm", "in"),
        infoDescription = "Precipitation amount in millimeters and inches."
    )

    // 12. Cloud cover
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_cloud_cover),
        value = "${weather?.current?.cloud ?: "?"}%",
        infoDescription = "Cloud cover as a percentage of sky coverage."
    )

    // 13. UV index with mini gauge visual
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = deviceInfoSpacing8, vertical = deviceInfoSpacing4),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DeviceInfoValueRow(
                index = index++,
                iconRes = iconRes,
                label = stringResource(R.string.device_info_label_weather_uv_index),
                value = parsed.uvIndex?.toString() ?: stringResource(R.string.device_info_unknown),
                infoDescription = "UV index indicating strength of ultraviolet radiation${parsed.uvIndex?.let { uv -> " (${uvBucket(uv).name.lowercase().replace('_', ' ')})" } ?: ""}."
            )
            Spacer(modifier = Modifier.width(deviceInfoSpacing8))
            MiniUVGauge(uvIndex = parsed.uvIndex, modifier = Modifier.size(deviceInfoIconSize28))
        }
    }

    // 14. Visibility with mini bar visual (last row)
    Column(modifier = Modifier.fillMaxWidth()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_weather_visibility),
            value = formatWeatherInfo(weather?.current?.visKm, weather?.current?.visMiles, "km", "mi"),
            infoDescription = "Visibility distance in kilometers and miles.",
            showBottomDivider = false
        )
        Spacer(modifier = Modifier.height(deviceInfoSpacing4))
        MiniVisibilityBar(
            visibilityKm = parsed.visibilityKm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = deviceInfoSpacing12)
        )
    }
}
