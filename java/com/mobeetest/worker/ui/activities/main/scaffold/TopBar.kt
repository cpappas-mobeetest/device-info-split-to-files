package com.mobeetest.worker.ui.activities.main.scaffold

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mobeetest.worker.R
import com.mobeetest.worker.ui.theme.DividerLineColor
import com.mobeetest.worker.ui.theme.MobeetestTheme
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarBackground
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarBottomBorder
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarBottomBorderHeight
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarBottomShadow
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarBottomShadowHeight
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarHeight
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarIcon
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarLogoPadding
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarLogoSize
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarMenuIconSize
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarMoreIconSize
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarTitlePadding
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarTitleTextStyle
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarTitleWidth
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarText
import com.mobeetest.worker.ui.theme.mainScaffoldTopBarZIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.wrapContentSize
import com.mobeetest.worker.ui.activities.permissions.pages.composables.BorderedAlertDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    currentScreen: String,
    onScreenSelected: (String) -> Unit,
    viewModels: List<Any>,
    drawerState: DrawerState,
    coroutineScope: CoroutineScope,
    onRecheckPermissions: () -> Unit
) {

    var expanded by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val titleMap = mapOf(
        "Network" to "Network Overview",
        "Neighbours" to "Network neighbours",
        "Graphs" to "Network Graphs",
        "Open Street Map" to "Open Street Map",
        "Speed Test" to "Speed Test",
        "Cellular" to "Network cellular & telephone information",
        "Driver Statistics" to "Network driver statistics",
        "Wi-Fi" to "Wi-Fi Information"
    )

    Column(modifier = Modifier.statusBarsPadding()) {
        Row {
            // Hard 1.dp black line (top separator)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DividerLineColor)
            )
        }
        Row {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(mainScaffoldTopBarZIndex),
                color = mainScaffoldTopBarBackground
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(mainScaffoldTopBarHeight)
                        .padding(horizontal = 0.dp)
                        .zIndex(mainScaffoldTopBarZIndex),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(mainScaffoldTopBarLogoSize)
                            .padding(mainScaffoldTopBarLogoPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = "App Logo",
                        )
                    }

                    // Drawer menu button
                    IconButton(
                        onClick = { coroutineScope.launch { drawerState.open() } },
                        modifier = Modifier.size(mainScaffoldTopBarMenuIconSize)
                    ) {
                        Icon(
                            Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = mainScaffoldTopBarIcon
                        )
                    }

                    // Title
                    Box(
                        modifier = Modifier
                            .padding(mainScaffoldTopBarTitlePadding)
                            .size(mainScaffoldTopBarTitleWidth),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = titleMap[currentScreen] ?: currentScreen,
                            style = mainScaffoldTopBarTitleTextStyle,
                            color = mainScaffoldTopBarText
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // More button + dropdown
                    Box(
                        modifier = Modifier
                            .size(mainScaffoldTopBarMoreIconSize)
                            .padding(end = 6.dp)
                            .wrapContentSize(Alignment.TopEnd)
                    ) {
                        IconButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                ImageVector.vectorResource(id = R.drawable.ic_more_vert),
                                contentDescription = "More",
                                tint = mainScaffoldTopBarIcon,
                                modifier = Modifier.size(mainScaffoldTopBarMoreIconSize)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Recheck Permissions") },
                                onClick = {
                                    expanded = false
                                    showResetDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Device Information") },
                                onClick = {
                                    expanded = false
                                    onScreenSelected("Device Information")
                                }
                            )
                        }
                    }
                    if (showResetDialog) {
                        BorderedAlertDialog(
                            title = "Recheck Permissions",
                            message = "This will clear all saved permission state and restart the app. " +
                                    "On next launch, permissions will be checked again as on first install.",
                            positiveButtonText = "Restart",
                            cancelable = true,
                            onPositiveClick = {
                                onRecheckPermissions()
                            },
                            onDismiss = {
                                showResetDialog = false
                            }
                        )
                    }


                }
            }
            // Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mainScaffoldTopBarBottomBorderHeight)
                    .background(mainScaffoldTopBarBottomBorder)
            )
            // Shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(mainScaffoldTopBarBottomShadowHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                mainScaffoldTopBarBottomShadow,
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun TopBarPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    MobeetestTheme {
        TopBar(
            currentScreen = "Network",
            onScreenSelected = {},
            viewModels = listOf(),
            drawerState = drawerState,
            coroutineScope = coroutineScope,
            onRecheckPermissions = {}
        )
    }
}
