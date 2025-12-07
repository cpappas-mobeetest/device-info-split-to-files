package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.ScreenInfo
import java.util.Locale

@Composable
fun ScreenSectionFields(screen: ScreenInfo, iconRes: Int) {
    var index = 1

    val orientationLabel = when (screen.orientation) {
        0 -> "0 (Portrait)"
        1 -> "90 (Landscape)"
        2 -> "180 (Reverse portrait)"
        3 -> "270 (Reverse landscape)"
        else -> screen.orientation.toString()
    }

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Screen class",
        value = screen.screenClass,
        infoDescription = "Logical size bucket of the screen (for example small, normal, large)."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Density class",
        value = screen.densityClass ?: "Unknown",
        infoDescription = "Density bucket used by Android resources (mdpi, hdpi, xhdpi, etc.)."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Visible size (px)",
        value = "${screen.widthPx} x ${screen.heightPx}",
        infoDescription = "Current visible size of the window in raw pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Visible size (dp)",
        value = "${screen.dpWidth} x ${screen.dpHeight}",
        infoDescription = "Current visible size of the window in density-independent pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Absolute size (px)",
        value = "${screen.absoluteWidthPx} x ${screen.absoluteHeightPx}",
        infoDescription = "Absolute physical display resolution in raw pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Absolute size (dp)",
        value = "${screen.absoluteDpWidth} x ${screen.absoluteDpHeight}",
        infoDescription = "Absolute physical display resolution converted to density-independent pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Density",
        value = String.format(Locale.US, "%.2f", screen.density),
        infoDescription = "Logical density of the display used to scale dp units to pixels."
    )

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Refresh rate",
        value = String.format(Locale.US, "%.1f Hz", screen.refreshRateHz),
        infoDescription = "Approximate display refresh rate in hertz."
    )

    DeviceInfoValueRow(
        index = index,
        iconRes = iconRes,
        label = "Orientation",
        value = orientationLabel,
        infoDescription = "Current screen orientation in degrees relative to the natural orientation.",
        showBottomDivider = false
    )
}
