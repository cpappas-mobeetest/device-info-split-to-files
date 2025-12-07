package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mobeetest.worker.ui.theme.deviceInfoFieldBackgroundEven
import com.mobeetest.worker.ui.theme.deviceInfoFieldBackgroundOdd

@Composable
internal fun deviceInfoFieldBackground(index: Int): Color {
    return if (index % 2 == 0) {
        deviceInfoFieldBackgroundEven
    } else {
        deviceInfoFieldBackgroundOdd
    }
}
