package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
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
        ActionIconSlot(
            touchSize = deviceInfoIconSize28,
            onClick = onRefreshClick
        ) {
            PlayGifAtLeastWhileInProgress(
                resId = R.drawable.update_gif,
                minPlays = 1,
                inProgress = (updateState == "InProgress"),
                contentDescription = "Refresh device info",
                onFinished = {}
            )
        }

        // Share icon
        ActionIconSlot(
            touchSize = deviceInfoIconSize28,
            onClick = { onShareClick(sectionId) }
        ) {
            androidx.compose.material3.Icon(
                painter = androidx.compose.ui.res.painterResource(id = R.drawable.share),
                contentDescription = "Share $sectionId info",
                tint = androidx.compose.ui.graphics.Color.Unspecified,
                modifier = Modifier.size(deviceInfoIconSize24)
            )
        }
    }
}
