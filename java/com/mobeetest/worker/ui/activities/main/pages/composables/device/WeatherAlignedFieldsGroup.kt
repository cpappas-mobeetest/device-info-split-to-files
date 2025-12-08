package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.mobeetest.worker.R
import com.mobeetest.worker.models.WeatherInfo
import java.util.Locale

@Composable
fun WeatherAlignedFieldsGroup(
    startIndex: Int,
    weather: WeatherInfo,
    iconForTemperature: (Double?) -> Int
) {
    val textMeasurer = rememberTextMeasurer()
    
    // Extract values
    val temperatureC = weather.current?.tempC
    val feelsLikeC = weather.current?.feelslikeC
    val windKph = weather.current?.windKph
    val windMph = weather.current?.windMph
    val windDir = weather.current?.windDir
    val humidity = weather.current?.humidity
    
    // Determine icon for feels like (same logic as temperature)
    val feelsLikeIconRes = iconForTemperature(feelsLikeC)
    val temperatureIconRes = iconForTemperature(temperatureC)
    
    // Calculate dynamic text width by measuring all 4 fields
    val dimensions = remember(temperatureC, feelsLikeC, windKph, windDir, humidity) {
        val tempText = buildAnnotatedString {
            append(stringResource(R.string.device_info_label_weather_temperature))
            append(": ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(String.format(Locale.US, "%.1f °C", temperatureC ?: 0.0))
            }
        }
        
        val feelsText = buildAnnotatedString {
            append(stringResource(R.string.device_info_label_weather_feels_like))
            append(": ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(String.format(Locale.US, "%.1f °C", feelsLikeC ?: 0.0))
            }
        }
        
        val windText = buildAnnotatedString {
            append(stringResource(R.string.device_info_label_weather_wind))
            append(": ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(String.format(Locale.US, "%.1f kph (%.1f mph) %s", 
                    windKph ?: 0.0, windMph ?: 0.0, windDir ?: ""))
            }
        }
        
        val humidityText = buildAnnotatedString {
            append(stringResource(R.string.device_info_label_weather_humidity))
            append(": ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("${humidity ?: 0}%")
            }
        }
        
        // Measure all texts and find max width
        val tempWidth = textMeasurer.measure(tempText).size.width
        val feelsWidth = textMeasurer.measure(feelsText).size.width
        val windWidth = textMeasurer.measure(windText).size.width
        val humidityWidth = textMeasurer.measure(humidityText).size.width
        
        val maxWidthPx = maxOf(tempWidth, feelsWidth, windWidth, humidityWidth)
        val textWidthDp = (maxWidthPx / 3.0).dp // Approximate px to dp conversion
        
        Pair(textWidthDp, 96.dp) // Fixed 96dp for visualizations
    }
    
    val (textWidth, visualWidth) = dimensions
    
    // Temperature
    WeatherAlignedTemperatureRow(
        index = startIndex,
        iconRes = temperatureIconRes,
        label = stringResource(R.string.device_info_label_weather_temperature),
        temperatureC = temperatureC ?: 0.0,
        textWidth = textWidth,
        visualWidth = visualWidth,
        thermometerHeight = 80.dp,
        infoDescription = "Current ambient temperature with thermometer visualization."
    )
    
    // Feels like
    WeatherAlignedTemperatureRow(
        index = startIndex + 1,
        iconRes = feelsLikeIconRes,
        label = stringResource(R.string.device_info_label_weather_feels_like),
        temperatureC = feelsLikeC ?: 0.0,
        textWidth = textWidth,
        visualWidth = visualWidth,
        thermometerHeight = 80.dp,
        infoDescription = "Perceived temperature accounting for wind chill and humidity with thermometer visualization."
    )
    
    // Wind
    WeatherAlignedWindRow(
        index = startIndex + 2,
        iconRes = R.drawable.weather_wind,
        label = stringResource(R.string.device_info_label_weather_wind),
        windKph = windKph ?: 0.0,
        windDir = windDir ?: "",
        textWidth = textWidth,
        visualWidth = visualWidth,
        infoDescription = "Wind speed and direction with compass visualization."
    )
    
    // Humidity
    WeatherAlignedHumidityRow(
        index = startIndex + 3,
        iconRes = R.drawable.weather_humidity,
        label = stringResource(R.string.device_info_label_weather_humidity),
        percentage = (humidity ?: 0).toFloat(),
        textWidth = textWidth,
        visualWidth = visualWidth,
        infoDescription = "Relative humidity percentage with donut visualization."
    )
}
