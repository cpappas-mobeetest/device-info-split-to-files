package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.UsbInfo

@Composable
fun UsbSectionFields(usb: UsbInfo, iconRes: Int) {
    val index = 1

    DeviceInfoBooleanFieldRow(
        index = index,
        iconRes = iconRes,
        label = "USB OTG / Host support",
        value = usb.otg,
        infoDescription = "Indicates whether the device can act as a USB host (OTG) for external peripherals.",
        showBottomDivider = false
    )
}
