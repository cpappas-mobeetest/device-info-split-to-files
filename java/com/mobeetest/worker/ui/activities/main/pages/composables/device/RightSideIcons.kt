package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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

    val icons = remember {
        listOf(
            // 1️⃣ Δεδομένα / formats (από πιο "raw" σε πιο "τελικό")
            R.drawable.json,
            R.drawable.csv,
            R.drawable.ms_excel,
            R.drawable.ms_word,
            R.drawable.pdf,
            R.drawable.png,

            // 2️⃣ Συσκευασία / μεταφορά
            R.drawable.zip,
            R.drawable.ftp,

            // 3️⃣ Ενέργειες πάνω στο αποτέλεσμα
            R.drawable.copy,

            // 4️⃣ Γενικές ενέργειες
            R.drawable.update,
            R.drawable.minimize
        )
    }

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Absolute.Center,
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.padding(vertical = 5.dp)
    ) {
        items(
            items = icons,
            key = { it }
        ) { icon ->
            // Update/Refresh icon with animated GIF support
            if (icon == R.drawable.update && playUpdateGif) {
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
                        if (icon == R.drawable.update) {
                            playUpdateGif = true
                            onRefreshClick()
                        }
                    }
                ) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(deviceInfoIconSize24)
                    )
                }
            }
        }
    }
}
