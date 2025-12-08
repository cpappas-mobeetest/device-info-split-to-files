package com.mobeetest.worker.ui.activities.permissions.scaffold

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.permissions.PermissionStatus
import com.mobeetest.worker.data.model.permissions.PermissionStep
import com.mobeetest.worker.ui.theme.*
import com.mobeetest.worker.viewModels.PermissionViewModel

@SuppressLint("ContextCastToActivity")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    playSoundAfterPermissionGranted: Boolean,
    viewModel: PermissionViewModel,
    playSound: Boolean,
    enablePermissionResetButton: Boolean,
    completedSteps: Int,
    totalSteps: Int,
    permissionSteps: List<PermissionStep>,
    enableSkipButton: Boolean,
    onReCheckAll: () -> Unit = {},
    onSkipPermissionCheck: () -> Unit = {},
) {
    Column(modifier = Modifier.statusBarsPadding()) {
        Row{
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
                    .background(permissionsScaffoldTopBarBackground)
                    .zIndex(permissionsScaffoldTopBarZIndex),
                color = MaterialTheme.colorScheme.primary
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(permissionsScaffoldTopBarHeight)
                        .padding(permissionsScaffoldTopBarPadding)
                        .background(permissionsScaffoldTopBarBackground)
                        .zIndex(permissionsScaffoldTopBarZIndex),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(permissionsScaffoldTopBarLogoSize)
                            .padding(permissionsScaffoldTopBarLogoPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = "App Logo",
                        )
                    }

                    // Title
                    Box(
                        modifier = Modifier
                            .padding(permissionsScaffoldTopBarTitlePadding)
                            .size(permissionsScaffoldTopBarTitleWidthPlay),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = stringResource(R.string.permissions_screen_title),
                            style = permissionsScaffoldTopBarTypography.bodyLarge,
                            color = permissionsScaffoldTopBarText
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    // Toggle sound
                    if (playSoundAfterPermissionGranted) {
                        IconButton(
                            onClick = { viewModel.setPlaySound(!playSound) },
                            modifier = Modifier
                                .size(permissionsScaffoldTopBarIconsSize)
                                .padding(permissionsScaffoldTopBarIconPadding)
                        ) {
                            Icon(
                                painter = painterResource(id = if (playSound) R.drawable.volume else R.drawable.mute),
                                contentDescription = null,
                                modifier = Modifier.size(permissionsScaffoldTopBarIconsSize),
                                tint = permissionsScaffoldTopBarIcon
                            )
                        }
                    }

                    // Reset (μόνο αν τελείωσαν όλα και υπάρχουν Denied)
                    if (enablePermissionResetButton) {
                        if (completedSteps == totalSteps && permissionSteps.any { it.status != PermissionStatus.Granted }) {
                            IconButton(
                                onClick = onReCheckAll,
                                modifier = Modifier
                                    .size(permissionsScaffoldTopBarIconsSize)
                                    .padding(permissionsScaffoldTopBarIconPadding)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.reset),
                                    contentDescription = stringResource(R.string.permissions_screen_reset_contentDescription),
                                    modifier = Modifier.size(permissionsScaffoldTopBarIconsSize),
                                    tint = permissionsScaffoldTopBarIcon
                                )
                            }
                        }
                    }

                    // Skip
                    if (enableSkipButton) {
                        IconButton(
                            onClick = onSkipPermissionCheck,
                            modifier = Modifier
                                .size(permissionsScaffoldTopBarIconsSize)
                                .padding(permissionsScaffoldTopBarIconPadding)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.skip),
                                contentDescription = stringResource(R.string.permissions_screen_skip_contentDescription),
                                modifier = Modifier.size(permissionsScaffoldTopBarIconsSize),
                                tint = permissionsScaffoldTopBarIcon
                            )
                        }
                    }
                }
            }
            // Border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(permissionsScaffoldTopBarBottomBorderHeight)
                    .background(permissionsScaffoldTopBarBottomBorder)
            )
            // Shadow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(permissionsScaffoldTopBarBottomShadowHeight)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                permissionsScaffoldTopBarBottomShadow,
                                Color.Transparent
                            )
                        )
                    )
            )
            // Hard 1.dp black line (top separator)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.Black.copy(alpha = 0.12f))
            )
        }
    }
}
