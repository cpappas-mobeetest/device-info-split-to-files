package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.models.WeatherInfo

@Composable
fun WeatherSectionFields(weather: WeatherInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_location),
        value = "${weather.location?.name ?: stringResource(R.string.device_info_unknown)}, ${weather.location?.country ?: ""}",
        infoDescription = "Location name and country from weather data provider."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_coordinates),
        value = "${weather.location?.lat ?: "?"}, ${weather.location?.lon ?: "?"}",
        infoDescription = "Geographic coordinates (latitude, longitude) of the location."
    )

    DeviceInfoDateTimeFieldRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_last_updated),
        millis = parseWeatherTime(weather.current?.lastUpdatedEpoch, weather.current?.lastUpdated)
            ?: System.currentTimeMillis(),
        infoDescription = "Timestamp when weather data was last updated by the provider."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_condition),
        value = weather.current?.condition?.text ?: stringResource(R.string.device_info_unknown),
        infoDescription = "Current weather condition description."
    )

    // Temperature row with thermometer visualization
    WeatherAlignedTemperatureRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_temperature),
        temperatureC = weather.current?.tempC ?: 0.0,
        textWidth = 150.dp,
        visualWidth = 80.dp,
        thermometerHeight = 80.dp,
        infoDescription = "Current temperature in degrees Celsius with thermometer visualization."
    )

    // Feels like temperature row with thermometer visualization
    weather.current?.feelslikeC?.let { feelslikeTemp ->
        WeatherAlignedTemperatureRow(
            index = index++,
            iconRes = iconRes,
            label = "Feels like",
            temperatureC = feelslikeTemp,
            textWidth = 150.dp,
            visualWidth = 80.dp,
            thermometerHeight = 80.dp,
            infoDescription = "Perceived temperature accounting for wind chill and humidity."
        )
    }

    // Humidity row with visualization
    WeatherAlignedHumidityRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_humidity),
        percentage = (weather.current?.humidity ?: 0).toFloat(),
        textWidth = 120.dp,
        visualWidth = 80.dp,
        infoDescription = "Current humidity percentage with donut visualization."
    )

    // Wind row with compass visualization
    WeatherAlignedWindRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_wind),
        windKph = weather.current?.windKph ?: 0.0,
        windDir = weather.current?.windDir,
        textWidth = 120.dp,
        visualWidth = 80.dp,
        infoDescription = "Current wind speed and direction with compass visualization."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_pressure),
        value = formatWeatherInfo(weather.current?.pressureMb, weather.current?.pressureIn, "mb", "in"),
        infoDescription = "Atmospheric pressure in millibars and inches of mercury."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_precipitation),
        value = formatWeatherInfo(weather.current?.precipMm, weather.current?.precipIn, "mm", "in"),
        infoDescription = "Precipitation amount in millimeters and inches."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_cloud_cover),
        value = "${weather.current?.cloud ?: "?"}%",
        infoDescription = "Cloud cover as a percentage of sky coverage."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_uv_index),
        value = weather.current?.uv?.toString() ?: stringResource(R.string.device_info_unknown),
        infoDescription = "UV index indicating strength of ultraviolet radiation."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_visibility),
        value = formatWeatherInfo(weather.current?.visKm, weather.current?.visMiles, "km", "mi"),
        infoDescription = "Visibility distance in kilometers and miles.",
        showBottomDivider = false
    )
}
