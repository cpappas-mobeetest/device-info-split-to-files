package com.mobeetest.worker.ui.activities.main.scaffold

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mobeetest.worker.ui.theme.mainDrawerVerticalPadding
import com.mobeetest.worker.ui.theme.mainDrawerWidth

// ------------ Public API ------------
@Composable
fun MobeetestDrawer(
    pageName: String,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Box(modifier = modifier.fillMaxSize()) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .width(mainDrawerWidth)
                .padding(vertical = mainDrawerVerticalPadding)
        ) {
            // TODO: drawer items
        }
    }
}
