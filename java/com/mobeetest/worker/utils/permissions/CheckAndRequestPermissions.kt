package com.mobeetest.worker.utils.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import androidx.core.content.PackageManagerCompat
import androidx.core.content.UnusedAppRestrictionsConstants.API_30
import androidx.core.content.UnusedAppRestrictionsConstants.API_30_BACKPORT
import androidx.core.content.UnusedAppRestrictionsConstants.API_31
import androidx.core.content.UnusedAppRestrictionsConstants.DISABLED
import androidx.core.content.UnusedAppRestrictionsConstants.ERROR
import androidx.core.content.UnusedAppRestrictionsConstants.FEATURE_NOT_AVAILABLE
import androidx.core.net.toUri
import com.google.common.util.concurrent.ListenableFuture
import com.mobeetest.worker.R
import com.mobeetest.worker.data.model.permissions.PermissionContinuations
import com.mobeetest.worker.data.model.permissions.PermissionDialogRequest
import com.mobeetest.worker.data.model.permissions.PermissionLaunchers
import com.mobeetest.worker.data.model.permissions.PermissionStatus
import com.mobeetest.worker.sharedPreferences.permissions.SharedPrefsManager
import java.util.concurrent.ExecutionException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow



/**
 * Permissions checks, alert dialog, and permission request.
 * Communicates με PermissionsActivity μέσω:
 * - callback updatePermissionStatus
 * - dialogState (για να ανοίγει Compose dialog)
 */
class CheckAndRequestPermissions {

    // Map to store permission statuses
    val permissionStatuses = mutableMapOf<String, PermissionStatus>()

    private lateinit var updatePermissionStatus: (permissionName: String, status: PermissionStatus) -> Unit
    private lateinit var context: Context
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var continuations: PermissionContinuations
    private lateinit var launchers: PermissionLaunchers
    private lateinit var openSettingsForPermission: String
    private var shouldReset: Boolean = false

    private var backgroundPermissionJob: Job? = null
    private var toastMessageDelay: String = "3500"
    private var permissionPollInterval: Long = 100L

    // ---- Dialog state για Compose ----
    private val _dialogState = MutableStateFlow<PermissionDialogRequest?>(null)
    val dialogState: StateFlow<PermissionDialogRequest?> get() = _dialogState

    private fun showPermissionDialog(request: PermissionDialogRequest) {
        _dialogState.value = request
    }

    fun clearDialog() {
        _dialogState.value = null
    }

    // ----------- Initialization / Reset -----------

    fun initializePermissions(
        context: Context,
        coroutineScope: CoroutineScope,
        continuations: PermissionContinuations,
        launchers: PermissionLaunchers,
        updatePermissionStatus: (permissionName: String, status: PermissionStatus) -> Unit,
        shouldReset: Boolean = false
    ) {
        this.context = context
        this.coroutineScope = coroutineScope
        this.continuations = continuations
        this.launchers = launchers
        this.shouldReset = shouldReset

        this.updatePermissionStatus = { permissionName, status ->
            // Update the map
            permissionStatuses[permissionName] = status
            // Call the original callback
            updatePermissionStatus(permissionName, status)
        }

        resetInternalState()
    }

    fun resetInternalState() {
        permissionStatuses.clear()
        permissionStatuses["Battery"] = PermissionStatus.NotChecked
        permissionStatuses["Location"] = PermissionStatus.NotChecked
        permissionStatuses["Notifications"] = PermissionStatus.NotChecked
        permissionStatuses["Phone"] = PermissionStatus.NotChecked
        permissionStatuses["Background"] = PermissionStatus.NotChecked
        permissionStatuses["Camera"] = PermissionStatus.NotChecked
        permissionStatuses["Pause"] = PermissionStatus.NotChecked
        permissionStatuses["Wifi"] = PermissionStatus.NotChecked
    }

    // ----------- Global check & shared preferences -----------

    @SuppressLint("UseKtx")
    fun checkAllPermission(con: Context? = null): Boolean {
        val rightCon = con ?: context

        // Battery
        val batteryOk = checkIgnoreBatteryOptimizationsPermission(rightCon)
        SharedPrefsManager.setLastBatteryGranted(rightCon, batteryOk)

        // Location
        val locOk = checkAccessAndCoarseLocation(rightCon)
        SharedPrefsManager.setLastLocationGranted(rightCon, locOk)

        // Notifications
        val notiOk = checkPostNotificationsPermission(rightCon)
        SharedPrefsManager.setLastNotificationsGranted(rightCon, notiOk)

        // Phone
        val phoneOk = checkIfReadPhoneStateIsGranted(rightCon)
        SharedPrefsManager.setLastPhoneGranted(rightCon, phoneOk)

        // Background location
        val bgOk = checkBackgroundLocation(rightCon)
        SharedPrefsManager.setLastBackgroundGranted(rightCon, bgOk)

        // Camera
        val cameraOk = checkCameraPermission(rightCon)
        SharedPrefsManager.setLastCameraGranted(rightCon, cameraOk)

        // Pause / Unused app restrictions
        val pauseOk = checkPauseAppWhenUnused(rightCon)
        SharedPrefsManager.setLastPauseGranted(rightCon, pauseOk)

        // Wifi
        val wifiOk = checkWifiPermission(rightCon)
        SharedPrefsManager.setLastWifiGranted(rightCon, wifiOk)

        return batteryOk && locOk && notiOk && phoneOk &&
                bgOk && cameraOk && pauseOk && wifiOk
    }

    // ----------- Public suspend check+request APIs -----------

    suspend fun checkAndRequestBatteryOptimizationsPermission(): PermissionStatus? {
        if (!checkIgnoreBatteryOptimizationsPermission(context)) {
            updatePermissionStatus("Battery", PermissionStatus.Requesting)
            permissionStatuses["Battery"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestIgnoreBattery()
            permissionStatuses["Battery"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Battery", PermissionStatus.Requesting)
                permissionStatuses["Battery"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Battery", PermissionStatus.Granted)
                permissionStatuses["Battery"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Battery", PermissionStatus.Granted)
                permissionStatuses["Battery"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Battery"]?.let { updatePermissionStatus("Battery", it) }
        return permissionStatuses["Battery"]
    }

    suspend fun checkAndRequestLocationPermission(): PermissionStatus? {
        if (!checkAccessAndCoarseLocation(context)) {
            updatePermissionStatus("Location", PermissionStatus.Requesting)
            permissionStatuses["Location"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestAccessAndCoarseLocation()
            permissionStatuses["Location"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Location", PermissionStatus.Requesting)
                permissionStatuses["Location"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Location", PermissionStatus.Granted)
                permissionStatuses["Location"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Location", PermissionStatus.Granted)
                permissionStatuses["Location"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Location"]?.let { updatePermissionStatus("Location", it) }
        return permissionStatuses["Location"]
    }

    suspend fun checkAndRequestNotificationPermission(): PermissionStatus? {
        if (!checkPostNotificationsPermission(context)) {
            updatePermissionStatus("Notifications", PermissionStatus.Requesting)
            permissionStatuses["Notifications"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestPostNotificationsPermission()
            permissionStatuses["Notifications"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Notifications", PermissionStatus.Requesting)
                permissionStatuses["Notifications"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Notifications", PermissionStatus.Granted)
                permissionStatuses["Notifications"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Notifications", PermissionStatus.Granted)
                permissionStatuses["Notifications"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Notifications"]?.let { updatePermissionStatus("Notifications", it) }
        return permissionStatuses["Notifications"]
    }

    suspend fun checkAndRequestPhonePermission(): PermissionStatus? {
        if (!checkIfReadPhoneStateIsGranted(context)) {
            updatePermissionStatus("Phone", PermissionStatus.Requesting)
            permissionStatuses["Phone"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestPhoneStatePermission()
            permissionStatuses["Phone"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Phone", PermissionStatus.Requesting)
                permissionStatuses["Phone"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Phone", PermissionStatus.Granted)
                permissionStatuses["Phone"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Phone", PermissionStatus.Granted)
                permissionStatuses["Phone"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Phone"]?.let { updatePermissionStatus("Phone", it) }
        return permissionStatuses["Phone"]
    }

    suspend fun checkAndRequestBackgroundPermission(): PermissionStatus? {
        if (checkAccessAndCoarseLocation(context)) {
            if (!checkBackgroundLocation(context)) {
                updatePermissionStatus("Background", PermissionStatus.Requesting)
                permissionStatuses["Background"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                val result = requestBackgroundLocation()
                permissionStatuses["Background"] = result
            } else {
                if (shouldReset) {
                    updatePermissionStatus("Background", PermissionStatus.Requesting)
                    permissionStatuses["Background"] = PermissionStatus.Requesting
                    delay(toastMessageDelay.toLong())
                    updatePermissionStatus("Background", PermissionStatus.Granted)
                    permissionStatuses["Background"] = PermissionStatus.Granted
                } else {
                    updatePermissionStatus("Background", PermissionStatus.Granted)
                    permissionStatuses["Background"] = PermissionStatus.Granted
                }
            }
        } else {
            updatePermissionStatus("Background", PermissionStatus.Requesting)
            permissionStatuses["Background"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            updatePermissionStatus("Background", PermissionStatus.Denied)
            permissionStatuses["Background"] = PermissionStatus.Denied
        }
        permissionStatuses["Background"]?.let { updatePermissionStatus("Background", it) }
        return permissionStatuses["Background"]
    }

    suspend fun checkAndRequestCameraPermission(): PermissionStatus? {
        if (!checkCameraPermission(context)) {
            updatePermissionStatus("Camera", PermissionStatus.Requesting)
            permissionStatuses["Camera"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestCameraPermission()
            permissionStatuses["Camera"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Camera", PermissionStatus.Requesting)
                permissionStatuses["Camera"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Camera", PermissionStatus.Granted)
                permissionStatuses["Camera"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Camera", PermissionStatus.Granted)
                permissionStatuses["Camera"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Camera"]?.let { updatePermissionStatus("Camera", it) }
        return permissionStatuses["Camera"]
    }

    suspend fun checkAndRequestPauseAppWhenUnusedPermission(): PermissionStatus? {
        if (!checkPauseAppWhenUnused(context)) {
            updatePermissionStatus("Pause", PermissionStatus.Requesting)
            permissionStatuses["Pause"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestPauseAppActivityWhenUnusedPermission()
            permissionStatuses["Pause"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Pause", PermissionStatus.Requesting)
                permissionStatuses["Pause"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Pause", PermissionStatus.Granted)
                permissionStatuses["Pause"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Pause", PermissionStatus.Granted)
                permissionStatuses["Pause"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Pause"]?.let { updatePermissionStatus("Pause", it) }
        return permissionStatuses["Pause"]
    }

    suspend fun checkAndRequestWifiPermission(): PermissionStatus? {
        if (!checkWifiPermission(context)) {
            updatePermissionStatus("Wifi", PermissionStatus.Requesting)
            permissionStatuses["Wifi"] = PermissionStatus.Requesting
            delay(toastMessageDelay.toLong())
            val result = requestWifiPermission()
            permissionStatuses["Wifi"] = result
        } else {
            if (shouldReset) {
                updatePermissionStatus("Wifi", PermissionStatus.Requesting)
                permissionStatuses["Wifi"] = PermissionStatus.Requesting
                delay(toastMessageDelay.toLong())
                updatePermissionStatus("Wifi", PermissionStatus.Granted)
                permissionStatuses["Wifi"] = PermissionStatus.Granted
            } else {
                updatePermissionStatus("Wifi", PermissionStatus.Granted)
                permissionStatuses["Wifi"] = PermissionStatus.Granted
            }
        }
        permissionStatuses["Wifi"]?.let { updatePermissionStatus("Wifi", it) }
        return permissionStatuses["Wifi"]
    }

    // ----------- Pure check helpers -----------

    fun checkIgnoreBatteryOptimizationsPermission(context: Context = this.context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun checkAccessAndCoarseLocation(context: Context = this.context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPostNotificationsPermission(context: Context = this.context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkIfReadPhoneStateIsGranted(context: Context = this.context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkBackgroundLocation(context: Context = this.context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkCameraPermission(context: Context = this.context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPauseAppWhenUnused(context: Context): Boolean {
        val future: ListenableFuture<Int> =
            PackageManagerCompat.getUnusedAppRestrictionsStatus(context)
        return try {
            val status = future.get()

            when (status) {
                ERROR -> false
                FEATURE_NOT_AVAILABLE -> false
                DISABLED -> true
                API_30_BACKPORT, API_30, API_31 -> false
                else -> false
            }
        } catch (_: ExecutionException) {
            false
        } catch (_: InterruptedException) {
            false
        }
    }

    fun checkWifiPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.NEARBY_WIFI_DEVICES
        ) == PackageManager.PERMISSION_GRANTED
    }

    // ----------- Request helpers (suspendCancellableCoroutine) -----------

    @SuppressLint("BatteryOptimization", "BatteryLife")
    private suspend fun requestIgnoreBattery(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            continuations.batteryOptimization = continuation
            try {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                }

                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_battery_title),
                        message = context.getString(R.string.permissions_screen_alert_battery_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                launchers.batteryOptimization.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.batteryOptimization = null
                                }
                            }
                        }
                    )
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
                continuations.batteryOptimization = null
            }
        }

    private suspend fun requestAccessAndCoarseLocation(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            if (checkAccessAndCoarseLocation(context)) {
                continuation.resume(PermissionStatus.Granted)
                return@suspendCancellableCoroutine
            }

            val hasRequestedLocation = SharedPrefsManager.getRequestedLocation(context)

            if (!hasRequestedLocation) {
                SharedPrefsManager.setRequestedLocation(context, true)
            }

            val needsRationale = !hasRequestedLocation ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    )

            if (needsRationale) {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_location_title),
                        message = context.getString(R.string.permissions_screen_alert_location_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.fineAndCoarseLocation = continuation
                                launchers.fineAndCoarseLocation.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                                continuation.invokeOnCancellation {
                                    continuations.fineAndCoarseLocation = null
                                }
                            }
                        }
                    )
                )
            } else {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_location_title),
                        message = context.getString(R.string.permissions_screen_alert_location_message_after_many_denies),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.settings = continuation
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.fromParts("package", context.packageName, null)
                                    }
                                openSettingsForPermission = "Location"
                                launchers.settings.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.settings = null
                                }
                            }

                        }
                    )
                )
            }
        }

    private suspend fun requestPostNotificationsPermission(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            if (checkPostNotificationsPermission(context)) {
                continuation.resume(PermissionStatus.Granted)
                return@suspendCancellableCoroutine
            }

            val hasRequestedNotifications =
                SharedPrefsManager.getRequestedNotifications(context)

            if (!hasRequestedNotifications) {
                SharedPrefsManager.setRequestedNotifications(context, true)
            }

            val needsRationale = !hasRequestedNotifications ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.POST_NOTIFICATIONS
                    )

            if (needsRationale) {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_notifications_title),
                        message = context.getString(R.string.permissions_screen_alert_notifications_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.postNotifications = continuation
                                launchers.postNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
                                continuation.invokeOnCancellation {
                                    continuations.postNotifications = null
                                }
                            }
                        }
                    )
                )
            } else {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_notifications_title),
                        message = context.getString(R.string.permissions_screen_alert_notifications_title_after_many_denies),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.settings = continuation
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.fromParts("package", context.packageName, null)
                                    }
                                openSettingsForPermission = "Notifications"
                                launchers.settings.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.settings = null
                                }
                            }

                        }
                    )
                )
            }
        }

    private suspend fun requestPhoneStatePermission(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            if (checkIfReadPhoneStateIsGranted(context)) {
                continuation.resume(PermissionStatus.Granted)
                return@suspendCancellableCoroutine
            }

            val hasRequestedPhone =
                SharedPrefsManager.getRequestedPhone(context)

            if (!hasRequestedPhone) {
                SharedPrefsManager.setRequestedPhone(context, true)
            }

            val needsRationale = !hasRequestedPhone ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.READ_PHONE_STATE
                    )

            if (needsRationale) {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_phone_title),
                        message = context.getString(R.string.permissions_screen_alert_phone_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.phoneState = continuation
                                launchers.phoneState.launch(Manifest.permission.READ_PHONE_STATE)
                                continuation.invokeOnCancellation {
                                    continuations.phoneState = null
                                }
                            }
                        }
                    )
                )
            } else {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_phone_title),
                        message = context.getString(R.string.permissions_screen_alert_phone_title_after_many_denies),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.settings = continuation
                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.fromParts("package", context.packageName, null)
                                    }
                                openSettingsForPermission = "Phone"
                                launchers.settings.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.settings = null
                                }
                            }
                        }
                    )
                )
            }
        }

    private suspend fun requestBackgroundLocation(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            if (checkBackgroundLocation(context)) {
                continuation.resume(PermissionStatus.Granted)
                return@suspendCancellableCoroutine
            }

            val hasRequestedBackgroundLocation =
                SharedPrefsManager.getRequestedBackground(context)

            if (!hasRequestedBackgroundLocation) {
                SharedPrefsManager.setRequestedBackground(context, true)
            }

            val needsRationale = !hasRequestedBackgroundLocation ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )

            if (needsRationale) {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_background_title),
                        message = context.getString(R.string.permissions_screen_alert_background_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.backgroundLocation = continuation
                                launchers.backgroundLocation.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                continuation.invokeOnCancellation {
                                    continuations.backgroundLocation = null
                                }

                            }


                        }
                    )
                )
            } else {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_background_title),
                        message = context.getString(R.string.permissions_screen_alert_background_title_after_many_denies),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.settings = continuation

                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.fromParts("package", context.packageName, null)
                                    }
                                openSettingsForPermission = "Background"
                                launchers.settings.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.settings = null
                                }

                            }


                        }
                    )
                )
            }
        }

    private suspend fun requestCameraPermission(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            if (checkCameraPermission(context)) {
                continuation.resume(PermissionStatus.Granted)
                return@suspendCancellableCoroutine
            }

            val hasRequestedCamera = SharedPrefsManager.getRequestedCamera(context)

            if (!hasRequestedCamera) {
                SharedPrefsManager.setRequestedCamera(context, true)
            }

            val needsRationale = !hasRequestedCamera ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.CAMERA
                    )

            if (needsRationale) {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_camera_title),
                        message = context.getString(R.string.permissions_screen_alert_camera_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.camera = continuation

                                launchers.camera.launch(Manifest.permission.CAMERA)
                                continuation.invokeOnCancellation {
                                    continuations.camera = null
                                }

                            }


                        }
                    )
                )
            } else {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_camera_title),
                        message = context.getString(R.string.permissions_screen_alert_camera_title_after_many_denies),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.settings = continuation

                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.fromParts("package", context.packageName, null)
                                    }
                                openSettingsForPermission = "Camera"
                                launchers.settings.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.settings = null
                                }


                            }


                        }
                    )
                )
            }
        }

    private suspend fun requestPauseAppActivityWhenUnusedPermission(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            continuations.pauseAppWhenUnused = continuation
            try {
                val intent = IntentCompat.createManageUnusedAppRestrictionsIntent(
                    context,
                    context.packageName
                )

                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_pause_title),
                        message = context.getString(R.string.permissions_screen_alert_pause_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)

                                launchers.pauseAppWhenUnused.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.pauseAppWhenUnused = null
                                }



                            }


                        }
                    )
                )
            } catch (e: Exception) {
                continuation.resumeWithException(e)
                continuations.pauseAppWhenUnused = null
            }
        }

    private suspend fun requestWifiPermission(): PermissionStatus =
        suspendCancellableCoroutine { continuation ->
            if (checkWifiPermission(context)) {
                continuation.resume(PermissionStatus.Granted)
                return@suspendCancellableCoroutine
            }

            val hasRequestedWifi = SharedPrefsManager.getRequestedWifi(context)

            if (!hasRequestedWifi) {
                SharedPrefsManager.setRequestedWifi(context, true)
            }

            val needsRationale = !hasRequestedWifi ||
                    ActivityCompat.shouldShowRequestPermissionRationale(
                        context as Activity,
                        Manifest.permission.NEARBY_WIFI_DEVICES
                    )

            if (needsRationale) {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_wifi_title),
                        message = context.getString(R.string.permissions_screen_alert_wifi_message),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.wifi = continuation
                                launchers.wifi.launch(Manifest.permission.NEARBY_WIFI_DEVICES)
                                continuation.invokeOnCancellation {
                                    continuations.wifi = null
                                }
                            }
                        }
                    )
                )
            } else {
                showPermissionDialog(
                    PermissionDialogRequest(
                        title = context.getString(R.string.permissions_screen_alert_wifi_title),
                        message = context.getString(R.string.permissions_screen_alert_wifi_title_after_many_denies),
                        positiveButtonText = context.getString(R.string.permissions_screen_alert_dialog_ok_text),
                        cancelable = false,
                        onPositiveClick = {
                            clearDialog()
                            coroutineScope.launch {
                                delay(150)
                                continuations.settings = continuation

                                val intent =
                                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                        data =
                                            Uri.fromParts("package", context.packageName, null)
                                    }
                                openSettingsForPermission = "Wifi"
                                launchers.settings.launch(intent)
                                continuation.invokeOnCancellation {
                                    continuations.settings = null
                                }

                            }


                        }
                    )
                )
            }
        }

    // ----------- Activity result handlers -----------

    suspend fun onBatteryOptimizationResult() {
        continuations.batteryOptimization?.let { continuation ->
            if (continuation.isActive) {
                val timeout = toastMessageDelay.toLong()
                val interval = permissionPollInterval
                val startTime = System.currentTimeMillis()
                var status: PermissionStatus

                do {
                    status = if (checkIgnoreBatteryOptimizationsPermission(context)) {
                        PermissionStatus.Granted
                    } else {
                        PermissionStatus.Denied
                    }
                    if (status == PermissionStatus.Granted) break
                    delay(interval)
                } while (System.currentTimeMillis() - startTime < timeout)

                updatePermissionStatus("Battery", status)
                permissionStatuses["Battery"] = status
                continuation.resume(status)
            }
            continuations.batteryOptimization = null
        }
    }

    fun onBackgroundPermissionResult() {
        backgroundPermissionJob?.cancel()
        backgroundPermissionJob = coroutineScope.launch {
            delay(500)
            continuations.backgroundLocation?.let { continuation ->
                if (continuation.isActive) {
                    val status = if (checkBackgroundLocation(context)) {
                        PermissionStatus.Granted
                    } else {
                        PermissionStatus.Denied
                    }
                    continuation.resume(status)
                }
                continuations.backgroundLocation = null
            }
        }
    }

    fun onPauseAppWhenUnusedResult() {
        continuations.pauseAppWhenUnused?.let { continuation ->
            if (continuation.isActive) {
                val status =
                    if (checkPauseAppWhenUnused(context)) PermissionStatus.Granted else PermissionStatus.Denied
                updatePermissionStatus("Pause", status)
                permissionStatuses["Pause"] = status
                continuation.resume(status)
            }
            continuations.pauseAppWhenUnused = null
        }
    }

    fun onSettingsResult() {
        continuations.settings?.let { continuation ->
            if (continuation.isActive) {
                val status = when (openSettingsForPermission) {
                    "Location" ->
                        if (checkAccessAndCoarseLocation(context)) PermissionStatus.Granted else PermissionStatus.Denied

                    "Notifications" ->
                        if (checkPostNotificationsPermission(context)) PermissionStatus.Granted else PermissionStatus.Denied

                    "Phone" ->
                        if (checkIfReadPhoneStateIsGranted(context)) PermissionStatus.Granted else PermissionStatus.Denied

                    "Background" -> {
                        val st = if (checkBackgroundLocation(context)) {
                            PermissionStatus.Granted
                        } else {
                            PermissionStatus.Denied
                        }
                        permissionStatuses["Background"] = st
                        updatePermissionStatus("Background", st)
                        st
                    }

                    "Wifi" ->
                        if (checkWifiPermission(context)) PermissionStatus.Granted else PermissionStatus.Denied

                    "Camera" ->
                        if (checkCameraPermission(context)) PermissionStatus.Granted else PermissionStatus.Denied

                    else -> PermissionStatus.Denied
                }

                continuation.resume(status)
            }
            continuations.settings = null
        }
    }
}
