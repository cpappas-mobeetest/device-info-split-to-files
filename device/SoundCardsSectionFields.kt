package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.runtime.Composable

@Composable
fun SoundCardsSectionFields(
    soundCardCount: Int,
    iconRes: Int
) {
    DeviceInfoValueRow(
        index = 1,
        iconRes = iconRes,
        label = "Sound card count",
        value = soundCardCount.toString(),
        infoDescription = "Total number of logical sound cards / audio output devices reported by Android on this device.",
        showBottomDivider = false
    )
}
