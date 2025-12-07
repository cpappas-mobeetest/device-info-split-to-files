package com.mobeetest.worker.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable



@Composable
fun MobeetestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        typography = Typography,
        content = content
    )
}