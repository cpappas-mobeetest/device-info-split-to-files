package com.mobeetest.worker.ui.activities.main.scaffold

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.DividerLineColor
import com.mobeetest.worker.ui.theme.mainBottomBarBackground
import com.mobeetest.worker.ui.theme.mainBottomBarBorder
import com.mobeetest.worker.ui.theme.mainBottomBarBorderHeight
import com.mobeetest.worker.ui.theme.mainBottomBarHeight
import com.mobeetest.worker.ui.theme.mainBottomBarIconSize
import com.mobeetest.worker.ui.theme.mainBottomBarItemWidth
import com.mobeetest.worker.ui.theme.mainBottomBarSelectedItemBackground

@Composable
fun BottomNavigationBar(
    currentScreen: String,
    onScreenSelected: (String) -> Unit
) {
    val items = listOf(
        "Network" to R.drawable.network_overview,
        "Neighbours" to R.drawable.neighbor,
        "Graphs" to R.drawable.graph,
        "Google Map" to Icons.Default.LocationOn,
        "Speed Test" to R.drawable.ic_speedtest,
        "Cellular" to R.drawable.ic_cellular,
        "Driver Statistics" to R.drawable.press,
        "Wi-Fi" to R.drawable.ic_wifi,
        //"DynamicCellTableCapture" to R.drawable.ic_launcher_foreground
    )


            Column(modifier = Modifier.navigationBarsPadding()) {
                // Border line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mainBottomBarBorderHeight)
                        .background(mainBottomBarBorder)
                )

                // Nav bar surface
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mainBottomBarHeight),
                    color = mainBottomBarBackground,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items.forEach { (screen, icon) ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(mainBottomBarItemWidth)
                                    .background(
                                        if (currentScreen == screen) mainBottomBarSelectedItemBackground
                                        else Color.Transparent
                                    )
                                    .clickable { onScreenSelected(screen) },
                                contentAlignment = Alignment.Center
                            ) {
                                when (icon) {
                                    is Int -> Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = screen,
                                        modifier = Modifier.size(mainBottomBarIconSize),
                                        tint = Color.Unspecified
                                    )

                                    is ImageVector -> Icon(
                                        imageVector = icon,
                                        contentDescription = screen,
                                        modifier = Modifier.size(mainBottomBarIconSize),
                                        tint = Color.Unspecified
                                    )
                                }
                            }
                        }
                    }
        }
        // BOTTOM SEPARATOR touching nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(DividerLineColor)
        )
    }
}
