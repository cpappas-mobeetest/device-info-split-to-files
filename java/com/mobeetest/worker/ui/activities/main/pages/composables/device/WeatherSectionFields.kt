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

    // Coordinates with location icon and Google Maps link
    DeviceInfoCoordinatesRow(
        index = index++,
        iconRes = R.drawable.location,
        label = stringResource(R.string.device_info_label_weather_coordinates),
        latitude = weather.location?.lat,
        longitude = weather.location?.lon,
        infoDescription = "Geographic coordinates (latitude, longitude) with link to Google Maps."
    )

    // Last updated with calendar and clock visualization
    DeviceInfoDateTimeFieldRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_last_updated),
        millis = parseWeatherTime(weather.current?.lastUpdatedEpoch, weather.current?.lastUpdated)
            ?: System.currentTimeMillis(),
        valueOverride = formatDateTimeNoSeconds(
            parseWeatherTime(weather.current?.lastUpdatedEpoch, weather.current?.lastUpdated)
                ?: System.currentTimeMillis()
        ),
        infoDescription = "Timestamp when weather data was last updated by the provider."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_condition),
        value = weather.current?.condition?.text ?: stringResource(R.string.device_info_unknown),
        infoDescription = "Current weather condition description."
    )

    // ✅ Aligned fields (Temperature, Feels like, Wind, Humidity) with dynamic width
    WeatherAlignedFieldsGroup(
        startIndex = index,
        weather = weather,
        iconForTemperature = { temp ->
            when {
                temp == null -> iconRes
                temp <= 0.0 -> R.drawable.weather_temperature_freezing
                temp <= 15.0 -> R.drawable.weather_temperature_cold
                temp <= 28.0 -> R.drawable.weather_temperature_mild
                else -> R.drawable.weather_temperature_hot
            }
        }
    )
    index += 4

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

    // Cloud cover with donut visualization
    WeatherAlignedHumidityRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_cloud_cover),
        percentage = (weather.current?.cloud ?: 0).toFloat(),
        textWidth = 120.dp,
        visualWidth = 80.dp,
        infoDescription = "Cloud cover as a percentage of sky coverage with donut visualization."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_uv_index),
        value = weather.current?.uv?.toString() ?: stringResource(R.string.device_info_unknown),
        infoDescription = "UV index indicating strength of ultraviolet radiation."
    )

    // Visibility with bar visualization
    WeatherAlignedVisibilityRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_weather_visibility),
        visibilityKm = weather.current?.visKm ?: 0.0,
        visMiles = weather.current?.visMiles ?: 0.0,
        textWidth = 120.dp,
        visualWidth = 80.dp,
        infoDescription = "Visibility distance in kilometers and miles with bar visualization.",
        showBottomDivider = false
    )
}
