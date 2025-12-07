package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R

@Composable
fun WeatherSectionFields(weatherJson: String?, iconRes: Int) {
    val weather = remember(weatherJson) { parseWeatherInfo(weatherJson) }
    
    var index = 1

    // If weather parsing failed, show unavailable message
    if (weather == null || weather.location == null || weather.current == null) {
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

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_location),
        value = "${weather.location.name ?: stringResource(R.string.device_info_unknown)}, ${weather.location.country ?: ""}",
        infoDescription = "Location name and country from weather data provider."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_coordinates),
        value = "${weather.location.lat ?: "?"}, ${weather.location.lon ?: "?"}",
        infoDescription = "Geographic coordinates (latitude, longitude) of the location."
    )

    DeviceInfoDateTimeFieldRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_last_updated),
        millis = parseWeatherTime(weather.current.lastUpdatedEpoch, weather.current.lastUpdated)
            ?: System.currentTimeMillis(),
        infoDescription = "Timestamp when weather data was last updated by the provider."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_condition),
        value = weather.current.condition?.text ?: stringResource(R.string.device_info_unknown),
        infoDescription = "Current weather condition description."
    )

    // Temperature row with thermometer visualization
    WeatherAlignedTemperatureRow(
        index = index++,
        iconRes = iconRes,
        label = "Temperature",
        temperatureC = weather.current.tempC ?: 0.0,
        textWidth = deviceInfoSpacing96,
        visualWidth = deviceInfoSpacing96,
        infoDescription = "Current air temperature near the surface."
    )

    // Humidity row with visualization
    WeatherAlignedHumidityRow(
        index = index++,
        iconRes = iconRes,
        label = "Humidity",
        percentage = weather.current.humidity?.toFloat() ?: 0f,
        textWidth = deviceInfoSpacing96,
        visualWidth = deviceInfoSpacing96,
        infoDescription = "Relative humidity of the air, expressed as a percentage."
    )

    // Wind row with compass visualization
    WeatherAlignedWindRow(
        index = index++,
        iconRes = iconRes,
        label = "Wind",
        windKph = weather.current.windKph ?: 0.0,
        windDir = weather.current.windDir,
        textWidth = deviceInfoSpacing96,
        visualWidth = deviceInfoSpacing96,
        infoDescription = "Wind speed in kilometers per hour and main wind direction."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_pressure),
        value = formatWeatherInfo(weather.current.pressureMb, weather.current.pressureIn, "mb", "in"),
        infoDescription = "Atmospheric pressure in millibars and inches of mercury."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_precipitation),
        value = formatWeatherInfo(weather.current.precipMm, weather.current.precipIn, "mm", "in"),
        infoDescription = "Precipitation amount in millimeters and inches."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_cloud_cover),
        value = "${weather.current.cloud ?: "?"}%",
        infoDescription = "Cloud cover as a percentage of sky coverage."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_uv_index),
        value = weather.current.uv?.toString() ?: stringResource(R.string.device_info_unknown),
        infoDescription = "UV index indicating strength of ultraviolet radiation."
    )

    DeviceInfoValueRow(
        index = index,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_visibility),
        value = formatWeatherInfo(weather.current.visKm, weather.current.visMiles, "km", "mi"),
        infoDescription = "Visibility distance in kilometers and miles.",
        showBottomDivider = false
    )
}
