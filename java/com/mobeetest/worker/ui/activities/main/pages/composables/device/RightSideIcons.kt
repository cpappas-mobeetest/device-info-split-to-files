package com.mobeetest.worker.ui.activities.main.pages.composables.device

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
    updateInProgress: Boolean,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconSize = 26.dp
    
    // ✅ Κοινό/αυξημένο touch για ΟΛΑ (και άρα ίδιο ripple)
    val touchSize = iconSize + 8.dp
    
    // ✅ Update οπτικά μεγαλύτερο
    val updateVisualSize = iconSize + 8.dp

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
            key = { it } // ✅ σταθερό state ανά icon
        ) { icon ->
            ActionIconSlot(
                touchSize = touchSize,
                onClick = {
                    when (icon) {
                        R.drawable.update -> onRefreshClick()
                        R.drawable.ftp -> { /*...*/ }
                        R.drawable.json -> { /*...*/ }
                        R.drawable.ms_word -> { /*...*/ }
                        R.drawable.ms_excel -> { /*...*/ }
                        R.drawable.csv -> { /*...*/ }
                        R.drawable.pdf -> { /*...*/ }
                        R.drawable.png -> { /*...*/ }
                        R.drawable.zip -> { /*...*/ }
                        R.drawable.copy -> { /*...*/ }
                        R.drawable.minimize -> { /*...*/ }
                    }
                }
            ) {
                when (icon) {
                    R.drawable.update -> {
                        var playUpdateGif by rememberSaveable { mutableStateOf(false) }

                        LaunchedEffect(updateInProgress) {
                            if (updateInProgress) playUpdateGif = true
                        }

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
                                    @Suppress("ASSIGNED_VALUE_IS_NEVER_READ")
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
