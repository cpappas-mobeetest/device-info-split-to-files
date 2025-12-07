package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.device.GpuInfo

@Composable
fun GpuSectionFields(gpu: GpuInfo, iconRes: Int) {
    var index = 1

    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_vendor),
        value = gpu.vendor,
        infoDescription = "GPU vendor string as reported by the graphics driver."
    )
    DeviceInfoValueRow(
        index = index++,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_renderer),
        value = gpu.renderer,
        infoDescription = "Renderer name that usually includes the GPU model and driver."
    )
    if (gpu.glesVersion.isNotBlank()) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_opengl_es),
            value = gpu.glesVersion,
            infoDescription = "Highest supported OpenGL ES version for this device."
        )
    }
    if (gpu.vulkanVersion.isNotBlank()) {
        DeviceInfoValueRow(
            index = index++,
            iconRes = iconRes,
            label = stringResource(R.string.device_info_label_vulkan),
            value = gpu.vulkanVersion,
            infoDescription = "Highest supported Vulkan API version for this device."
        )
    }

    DeviceInfoTextListField(
        index = index,
        iconRes = iconRes,
        label = stringResource(R.string.device_info_label_extensions),
        values = gpu.extensions,
        maxPreviewItems = 6,
        infoDescription = "List of GPU driver extensions that are available to graphics applications.",
        showBottomDivider = false
    )
}
