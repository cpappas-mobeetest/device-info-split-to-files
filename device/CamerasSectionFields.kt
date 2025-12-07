package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.CameraInfo

@Composable
fun CamerasSectionFields(cameras: List<CameraInfo>, iconRes: Int) {
    var index = 1

    if (cameras.isEmpty()) {
        DeviceInfoValueRow(
            index = index,
            iconRes = iconRes,
            label = "Cameras",
            value = "None detected",
            infoDescription = "No camera devices were reported by the system."
        )
        return
    }

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Total cameras",
        value = cameras.size.toString(),
        infoDescription = "Total number of camera devices reported by Android (front, back or external)."
    )

    // Note: This requires DeviceInfoTextListField to be created
    // val entries = cameras.mapIndexed { camIndex, cam ->
    //     "Camera $camIndex: ${cam.type} (${cam.orientation}°)"
    // }
    // DeviceInfoTextListField(
    //     index = index,
    //     iconRes = iconRes,
    //     label = "Available cameras",
    //     values = entries,
    //     maxPreviewItems = 4,
    //     showBottomDivider = false,
    //     infoDescription = "List of all available cameras with their type and sensor orientation."
    // )
}
