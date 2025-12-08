package com.mobeetest.worker.ui.activities.main.pages.composables.device

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.BatteryInfo

@Composable
fun BatterySectionFields(
    b: BatteryInfo,
    iconRes: Int
) {
    var index = 1

    // 1. Battery level with donut
    val levelPercent = b.levelPercent?.coerceIn(0, 100)
    if (levelPercent != null) {
        val levelF = levelPercent.toFloat()
        val color = when {
            levelF < 15f -> MaterialTheme.colorScheme.error
            levelF < 30f -> MaterialTheme.colorScheme.tertiary
            else -> MaterialTheme.colorScheme.primary
        }

        DeviceInfoPercentageFieldRow(
            index = index++,
            iconRes = iconRes,
            label = "Battery level",
            percentage = levelF,
            progressColor = color,
            infoDescription = "Current battery charge level as a percentage of full capacity."
        )
    } else {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "Battery level",
            value = "Unknown",
            infoDescription = "Current battery charge level as a percentage of full capacity."
        )
    }

    // 2. Status
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Status",
        value = describeBatteryStatus(b.status),
        infoDescription = "Charging state of the battery as reported by Android."
    )

    // 3. Power source
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Power source",
        value = describeChargerConnection(b.chargerConnection),
        infoDescription = "Current power source: AC, USB, wireless charger or running on battery."
    )

    // 4. Health
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Health",
        value = describeBatteryHealth(b.health),
        infoDescription = "Battery health reported by the system (good, overheated, dead, etc.)."
    )

    // 5. Temperature - with thermometer visualization like Weather Temperature
    b.temperatureC?.let { temp ->
        WeatherAlignedTemperatureRow(
            index = index++,
            iconRes = iconForTemperature(temp.toDouble()),
            label = "Temperature",
            temperatureC = temp.toDouble(),
            textWidth = 120.dp,
            visualWidth = 80.dp,
            infoDescription = "Current battery temperature in degrees Celsius."
        )
    } ?: run {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "Temperature",
            value = "Unknown",
            infoDescription = "Current battery temperature in degrees Celsius."
        )
    }

    // 6. Capacity
    val capacityText = if (b.capacityMah > 0f) b.capacityMah.toInt().toString() else "Unknown"
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Battery capacity",
        value = capacityText,
        unit = if (b.capacityMah > 0f) "mAh" else null,
        infoDescription = "Approximate nominal battery capacity in milliampere-hours as reported by the system."
    )

    // 7. Technology (last row)
    DeviceInfoValueRow(
        index = index,
        iconRes = iconRes,
        label = "Technology",
        value = b.technology,
        infoDescription = "Battery chemistry string, for example Li-ion or Li-polymer.",
        showBottomDivider = false
    )
}

// Helper function to select temperature icon based on temperature value
// Uses temperatureBucket() and TemperatureBucket from WeatherModels.kt
private fun iconForTemperature(tempC: Double?): Int {
    return when (temperatureBucket(tempC)) {
        TemperatureBucket.FREEZING -> R.drawable.weather_temp_freezing
        TemperatureBucket.COLD -> R.drawable.weather_temp_cold
        TemperatureBucket.MILD -> R.drawable.weather_temp_mild
        TemperatureBucket.HOT -> R.drawable.weather_temp_hot
    }
}
