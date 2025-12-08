package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.mobeetest.worker.R

@Composable
fun RightSideIcons(
    updateState: String,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var playUpdateGif by remember { mutableStateOf(false) }

    LaunchedEffect(updateState) {
        playUpdateGif = (updateState == "InProgress")
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(deviceInfoSpacing8),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Update/Refresh icon with animated GIF support
        if (playUpdateGif) {
            PlayGifAtLeastWhileInProgress(
                resId = R.drawable.update_gif,
                minPlays = 1,
                inProgress = (updateState == "InProgress"),
                modifier = Modifier.size(deviceInfoIconSize24),
                contentDescription = "Refresh device info",
                onFinished = { playUpdateGif = false }
            )
        } else {
            ActionIconSlot(
                touchSize = deviceInfoIconSize24,
                onClick = {
                    playUpdateGif = true
                    onRefreshClick()
                }
            ) {
                androidx.compose.foundation.Image(
                    painter = painterResource(id = R.drawable.update),
                    contentDescription = "Refresh device info",
                    modifier = Modifier.size(deviceInfoIconSize24)
                )
            }
        }
    }
}
