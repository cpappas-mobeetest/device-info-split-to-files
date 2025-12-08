package com.mobeetest.worker.ui.activities.main.pages.composables.device

import com.mobeetest.worker.ui.theme.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
    val iconSize = 26.dp
    val touchSize = iconSize + 8.dp
    val updateVisualSize = iconSize + 8.dp
    val updateInProgress = (updateState == "InProgress")

    val icons = remember {
        listOf(
            R.drawable.json,
            R.drawable.csv,
            R.drawable.ms_excel,
            R.drawable.ms_word,
            R.drawable.pdf,
            R.drawable.png,
            R.drawable.zip,
            R.drawable.ftp,
            R.drawable.copy,
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
            // Store playUpdateGif state outside the when expression so it can be accessed in onClick
            var playUpdateGif by rememberSaveable(key = "update_gif_${icon}") { mutableStateOf(false) }
            
            if (icon == R.drawable.update) {
                LaunchedEffect(updateInProgress) {
                    if (updateInProgress) playUpdateGif = true
                }
            }
            
            ActionIconSlot(
                touchSize = touchSize,
                onClick = {
                    when (icon) {
                        R.drawable.update -> {
                            playUpdateGif = true
                            onRefreshClick()
                        }
                        R.drawable.ftp -> { /* TODO */ }
                        R.drawable.json -> { /* TODO */ }
                        R.drawable.ms_word -> { /* TODO */ }
                        R.drawable.ms_excel -> { /* TODO */ }
                        R.drawable.csv -> { /* TODO */ }
                        R.drawable.pdf -> { /* TODO */ }
                        R.drawable.png -> { /* TODO */ }
                        R.drawable.zip -> { /* TODO */ }
                        R.drawable.copy -> { /* TODO */ }
                        R.drawable.minimize -> { /* TODO */ }
                    }
                }
            ) {
                when (icon) {
                    R.drawable.update -> {

                        if (!playUpdateGif) {
                            Icon(
                                painter = painterResource(id = icon),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(updateVisualSize)
                            )
                        } else {
                            PlayGifAtLeastWhileInProgress(
                                resId = R.drawable.update_gif,
                                minPlays = 2,
                                inProgress = updateInProgress,
                                modifier = Modifier.size(updateVisualSize),
                                contentDescription = "Refresh",
                                onFinished = {
                                    playUpdateGif = false
                                }
                            )
                        }
                    }
                    else -> {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
        }
    }
}
