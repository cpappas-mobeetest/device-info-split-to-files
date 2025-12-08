package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Suppress("unused")
@Composable
fun WeatherAlignedFieldsGroup(
    startIndex: Int,
    temperatureC: Double?,
    feelsLikeC: Double?,
    windKph: Double?,
    windDir: String?,
    humidity: Int?,
    temperatureIconRes: Int,
    feelsLikeIconRes: Int,
    windIconRes: Int,
    humidityIconRes: Int
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val dimensions = remember(
        temperatureC,
        feelsLikeC,
        windKph,
        windDir,
        humidity,
        textMeasurer
    ) {
        val textStyle = TextStyle(
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        val texts = listOf(
            buildAnnotatedString {
                append("Temperature: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(String.format(Locale.US, "%.1f °C", temperatureC ?: 0.0))
                }
            },
            buildAnnotatedString {
                append("Feels like: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(String.format(Locale.US, "%.1f °C", feelsLikeC ?: 0.0))
                }
            },
            buildAnnotatedString {
                val windDirDisplay = windDir?.takeIf { it.isNotBlank() } ?: "?"
                val windValue = String.format(Locale.US, "%.1f kph", windKph ?: 0.0)
                append("Wind: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("$windValue ($windDirDisplay)")
                }
            },
            buildAnnotatedString {
                append("Humidity: ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                    append(String.format(Locale.US, "%.3f%%", (humidity ?: 0).toFloat()))
                }
            }
        )

        val maxWidthPx = texts.maxOf { text ->
            textMeasurer.measure(
                text = text,
                style = textStyle,
                maxLines = 1
            ).size.width
        }

        val textWidth = with(density) { maxWidthPx.toDp() }
        val visualWidth = 96.dp

        Pair(textWidth, visualWidth)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Temperature
        temperatureC?.let { temp ->
            WeatherAlignedTemperatureRow(
                index = startIndex,
                iconRes = temperatureIconRes,
                label = "Temperature",
                temperatureC = temp,
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Current air temperature near the surface."
            )
        }

        // Feels like
        feelsLikeC?.let { feels ->
            WeatherAlignedTemperatureRow(
                index = startIndex + 1,
                iconRes = feelsLikeIconRes,
                label = "Feels like",
                temperatureC = feels,
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Perceived temperature taking into account wind and humidity."
            )
        }

        // Wind
        windKph?.let { wind ->
            WeatherAlignedWindRow(
                index = startIndex + 2,
                iconRes = windIconRes,
                label = "Wind",
                windKph = wind,
                windDir = windDir,
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Wind speed in kilometers per hour and main wind direction."
            )
        }

        // Humidity
        humidity?.let { hum ->
            WeatherAlignedHumidityRow(
                index = startIndex + 3,
                iconRes = humidityIconRes,
                label = "Humidity",
                percentage = hum.toFloat(),
                textWidth = dimensions.first,
                visualWidth = dimensions.second,
                infoDescription = "Relative humidity of the air, expressed as a percentage."
            )
        }
    }
}
