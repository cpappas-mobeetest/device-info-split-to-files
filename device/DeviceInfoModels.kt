package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

internal data class DeviceInfoItem(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val children: List<DeviceInfoItem> = emptyList()
)
