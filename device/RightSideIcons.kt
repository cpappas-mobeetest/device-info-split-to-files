package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            inProgress = (updateState == "InProgress"),
            resId = R.drawable.update,
            gifIcon = R.drawable.update_gif,
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
