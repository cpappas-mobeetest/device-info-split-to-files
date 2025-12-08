package com.mobeetest.worker.ui.activities.permissions.pages.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.mobeetest.worker.R
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.LayoutDirection
import com.mobeetest.worker.data.model.permissions.PermissionStatus
import com.mobeetest.worker.data.model.permissions.PermissionStep
import com.mobeetest.worker.ui.activities.permissions.scaffold.Content
import com.mobeetest.worker.ui.activities.permissions.scaffold.TopBar
import com.mobeetest.worker.viewModels.PermissionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel as composeViewModel

@Composable
private fun getShortDisplayName(permission: String): String = when (permission) {
    "Battery" -> stringResource(R.string.permissions_screen_battery)
    "Location" -> stringResource(R.string.permissions_screen_location)
    "Notifications" -> stringResource(R.string.permissions_screen_notifications)
    "Phone" -> stringResource(R.string.permissions_screen_phone)
    "Background" -> stringResource(R.string.permissions_screen_background)
    "Pause" -> stringResource(R.string.permissions_screen_pause)
    "Wifi" -> stringResource(R.string.permissions_screen_wifi)
    else -> permission
}

@ExperimentalFoundationApi
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionsScreen(
    permissionSteps: List<PermissionStep>,
    onReCheckAll: () -> Unit = {},
    onSkipPermissionCheck: () -> Unit = {},
    viewModel: PermissionViewModel = composeViewModel()
) {
    val enablePermissionResetButton = true
    val enableSkipButton = true
    val playSoundAfterPermissionGranted = true
    val playSound by viewModel.playSound

    val currentPermission = permissionSteps.firstOrNull {
        it.status == PermissionStatus.NotChecked || it.status == PermissionStatus.Requesting
    }?.name ?: ""

    val totalSteps = permissionSteps.size
    val completedSteps = permissionSteps.count {
        it.status == PermissionStatus.Granted || it.status == PermissionStatus.Denied
    }

    val displayPermissionName = getShortDisplayName(currentPermission)

    val progressMessage = if (completedSteps < totalSteps) {
        stringResource(
            R.string.permissions_check_in_progress_full,
            completedSteps + 1,
            totalSteps,
            displayPermissionName
        )
    } else {
        if (permissionSteps.all { it.status == PermissionStatus.Granted }) {
            stringResource(R.string.permissions_check_ended_all_granted)
        } else {
            val deniedCount = permissionSteps.count { it.status == PermissionStatus.Denied }
            stringResource(R.string.permissions_check_ended_some_denied, deniedCount)
        }
    }

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding(),   // ⭐ Σηκώνει όλο το Scaffold πάνω από το nav bar
        topBar = {
            TopBar(
                playSoundAfterPermissionGranted = playSoundAfterPermissionGranted,
                viewModel = viewModel,
                playSound = playSound,
                enablePermissionResetButton = enablePermissionResetButton,
                completedSteps = completedSteps,
                totalSteps = totalSteps,
                permissionSteps = permissionSteps,
                enableSkipButton = enableSkipButton,
                onReCheckAll = onReCheckAll,
                onSkipPermissionCheck = onSkipPermissionCheck
            )
        },
        bottomBar = {

        },
        contentWindowInsets = WindowInsets(0)  // ⭐ Καταργεί τα default insets του Scaffold
    ) { padding ->

        Content(
            progressMessage = progressMessage,
            completedSteps = completedSteps,
            totalSteps = totalSteps,
            permissionSteps = permissionSteps,
            modifier = Modifier
                .padding(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(LayoutDirection.Ltr),
                    end = padding.calculateEndPadding(LayoutDirection.Ltr)
                    // ❗ κάτω padding ΔΕΝ βάζουμε
                )
        )
    }

}














