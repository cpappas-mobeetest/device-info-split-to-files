package com.mobeetest.worker.activities.main.pages.composables.device

internal data class DeviceInfoItem(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val children: List<DeviceInfoItem> = emptyList()
)
