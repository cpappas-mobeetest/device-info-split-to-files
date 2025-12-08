package com.mobeetest.worker.data.model.permissions

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CancellableContinuation

enum class PermissionStatus {
    NotChecked,
    Requesting,
    Granted,
    Denied
}

data class PermissionStep(
    val name: String,
    var status: PermissionStatus
)

data class PermissionLaunchers(
    var batteryOptimization: ActivityResultLauncher<Intent>,
    var fineAndCoarseLocation: ActivityResultLauncher<Array<String>>,
    var postNotifications: ActivityResultLauncher<String>,
    var phoneState: ActivityResultLauncher<String>,
    var backgroundLocation: ActivityResultLauncher<String>,
    var pauseAppWhenUnused: ActivityResultLauncher<Intent>,
    var camera: ActivityResultLauncher<String>,
    var wifi: ActivityResultLauncher<String>,
    var settings: ActivityResultLauncher<Intent>,
)

data class PermissionContinuations(
    var batteryOptimization: CancellableContinuation<PermissionStatus>?,
    var fineAndCoarseLocation: CancellableContinuation<PermissionStatus>?,
    var postNotifications: CancellableContinuation<PermissionStatus>?,
    var phoneState: CancellableContinuation<PermissionStatus>?,
    var backgroundLocation: CancellableContinuation<PermissionStatus>?,
    var pauseAppWhenUnused: CancellableContinuation<PermissionStatus>?,
    var camera: CancellableContinuation<PermissionStatus>?,
    var wifi: CancellableContinuation<PermissionStatus>?,
    var settings: CancellableContinuation<PermissionStatus>?,
)

data class PermissionDialogRequest(
    val title: String,
    val message: String,
    val positiveButtonText: String = "OK",
    val cancelable: Boolean = false,
    val onPositiveClick: () -> Unit
)
