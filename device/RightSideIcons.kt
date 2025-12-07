package com.mobeetest.worker.activities.main.pages.composables.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R

@Composable
fun RightSideIcons(
    updateState: String,
    onRefreshClick: () -> Unit,
    onShareClick: (String) -> Unit,
    sectionId: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(deviceInfoSpacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Update/Refresh icon with animated GIF support
        PlayGifAtLeastWhileInProgress(
            showGif = (updateState == "InProgress"),
            normalIcon = R.drawable.refresh,
            gifIcon = R.drawable.refresh_animated,
            contentDescription = "Refresh device info",
            onClick = onRefreshClick
        )

        // Share icon
        ActionIconSlot(
            iconRes = R.drawable.share,
            contentDescription = "Share $sectionId info",
            onClick = { onShareClick(sectionId) }
        )
    }
}
