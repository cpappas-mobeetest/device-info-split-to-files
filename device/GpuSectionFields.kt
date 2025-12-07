package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.runtime.Composable
import com.mobeetest.worker.data.model.device.GpuInfo

@Composable
fun GpuSectionFields(gpu: GpuInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Vendor",
        value = gpu.vendor,
        infoDescription = "GPU vendor string as reported by the graphics driver."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = "Renderer",
        value = gpu.renderer,
        infoDescription = "Renderer name that usually includes the GPU model and driver."
    )
    if (gpu.glesVersion.isNotBlank()) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "OpenGL ES",
            value = gpu.glesVersion,
            infoDescription = "Highest supported OpenGL ES version for this device."
        )
    }
    if (gpu.vulkanVersion.isNotBlank()) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = "Vulkan",
            value = gpu.vulkanVersion,
            infoDescription = "Highest supported Vulkan API version for this device."
        )
    }

    // Note: DeviceInfoTextListField needs to be created separately
    // DeviceInfoTextListField(
    //     index = index,
    //     iconRes = iconRes,
    //     label = "Extensions",
    //     values = gpu.extensions,
    //     maxPreviewItems = 6,
    //     infoDescription = "List of GPU driver extensions that are available to graphics applications.",
    //     showBottomDivider = false
    // )
}
