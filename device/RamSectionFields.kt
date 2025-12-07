package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.RamInfo

@Composable
fun RamSectionFields(ram: RamInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Total RAM",
        value = formatMegabytes(ram.totalMB),
        infoDescription = "Approximate total amount of system memory available on the device."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Available RAM",
        value = formatMegabytes(ram.availableMB),
        infoDescription = "Estimated free RAM currently available for apps and the system."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Low memory threshold",
        value = formatMegabytes(ram.thresholdMB),
        infoDescription = "Threshold below which Android considers the device to be in a low-memory state."
    )

    DeviceInfoPercentageFieldRow(
        index = index,
        iconRes = iconRes,
        label = "Available RAM",
        percentage = ram.availablePercent,
        unit = "%",
        progressColor = if (ram.availablePercent < 20f)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.primary,
        infoDescription = "Percentage of total RAM that is currently available (not in use).",
        showBottomDivider = false
    )
}
