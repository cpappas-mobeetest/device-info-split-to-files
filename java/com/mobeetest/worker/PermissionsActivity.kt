package com.mobeetest.worker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.mobeetest.worker.ui.theme.MobeetestTheme
import kotlinx.coroutines.launch
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.mobeetest.worker.data.model.permissions.PermissionContinuations
import com.mobeetest.worker.data.model.permissions.PermissionLaunchers
import com.mobeetest.worker.data.model.permissions.PermissionStatus
import com.mobeetest.worker.data.model.permissions.PermissionStep
import com.mobeetest.worker.utils.permissions.CheckAndRequestPermissions
import com.mobeetest.worker.utils.permissions.PermissionsStatusMp3
import com.mobeetest.worker.utils.permissions.SoundPlayer
import com.mobeetest.worker.viewModels.PermissionViewModel
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import com.mobeetest.worker.sharedPreferences.permissions.SharedPrefsManager
import com.mobeetest.worker.ui.activities.permissions.pages.composables.BorderedAlertDialog
import com.mobeetest.worker.ui.activities.permissions.pages.composables.CustomSnackbar
import com.mobeetest.worker.ui.activities.permissions.pages.screens.PermissionsScreen

@ExperimentalFoundationApi
class PermissionsActivity : ComponentActivity() {
    private val snackbarHostState = SnackbarHostState()

    private var permissionHandler: CheckAndRequestPermissions = CheckAndRequestPermissions()
    private var savedInstanceStateOld:Bundle? = null // used for background location permission because if use play around with the available options, then the PermissionsActivity is restarted and this cause problems
    private var enableDebugLogging:Boolean = false
    private var showPermissionStatusToast:Boolean = true
    private var playSoundAfterPermissionGranted:Boolean = true
    private var playSoundAfterPermissionGrantedSetting:Boolean = true
    private val soundPlayer: SoundPlayer = SoundPlayer(this@PermissionsActivity)
    private lateinit var viewModel: PermissionViewModel
    private var isPlaySoundEnabled:Boolean = true
    private val permissionSteps = mutableStateListOf(
        PermissionStep("Battery", PermissionStatus.NotChecked),
        PermissionStep("Location", PermissionStatus.NotChecked),
        PermissionStep("Notifications", PermissionStatus.NotChecked),
        PermissionStep("Phone", PermissionStatus.NotChecked),
        PermissionStep("Background", PermissionStatus.NotChecked),
        PermissionStep("Pause", PermissionStatus.NotChecked),
        PermissionStep("Camera", PermissionStatus.NotChecked),
        PermissionStep("Wifi", PermissionStatus.NotChecked),

    )

    private var batteryChecked:Boolean = false
    private var locationChecked:Boolean = false
    private var notificationsChecked:Boolean = false
    private var phoneChecked:Boolean = false
    private var backgroundChecked:Boolean = false
    private var pauseChecked:Boolean = false

    private var cameraChecked:Boolean = false
    private var wifiChecked:Boolean = false

    private fun onPermissionWizardFinished() {
        // Εδώ έχουν ολοκληρωθεί όλα τα βήματα

        val resultIntent = Intent().apply {
            putExtra(MainActivity.EXTRA_PERMISSIONS_CHECK_COMPLETED, true)
        }

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private val continuations = PermissionContinuations(
        batteryOptimization = null,
        fineAndCoarseLocation = null,
        postNotifications = null,
        phoneState = null,
        backgroundLocation = null,
        pauseAppWhenUnused = null,
        camera = null,
        wifi = null,
        settings = null
    )

    private val batteryOptimizationLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // Launch a coroutine to call the suspend function
            lifecycleScope.launch {
                permissionHandler.onBatteryOptimizationResult()
            }
        }

    private val requestFineAndCoarseLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
            val status = if (allGranted) PermissionStatus.Granted else PermissionStatus.Denied

            // Resume continuation from helper
            continuations.fineAndCoarseLocation?.let { cont ->
                if (cont.isActive) {
                    cont.resume(status) { _, _, _ -> }
                }
            }
            continuations.fineAndCoarseLocation = null

            // Update UI
            updatePermissionStatus("Location", status)
        }

    private val requestPostNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val status = if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied

            continuations.postNotifications?.let { cont ->
                if (cont.isActive) {
                    cont.resume(status) { _, _, _ -> }
                }
            }
            continuations.postNotifications = null

            updatePermissionStatus("Notifications", status)
        }

    private val requestPhoneStatePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (enableDebugLogging) {
                Log.d("MainActivity", "Read phone state permission request callback received: $isGranted")
            }
            val status = if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied

            continuations.phoneState?.let { cont ->
                if (cont.isActive) {
                    cont.resume(status) { _, _, _ -> }
                }
            }
            continuations.phoneState = null

            updatePermissionStatus("Phone", status)
        }

    private val requestBackgroundLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val status = if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied
            updatePermissionStatus("Background", status)
            // Debounced callback
            permissionHandler.onBackgroundPermissionResult()
        }

    private val requestPauseAppWhenUnusedPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            lifecycleScope.launch {
                permissionHandler.onPauseAppWhenUnusedResult()
            }
        }

    private val requestWifiPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val status = if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied

            continuations.wifi?.let { cont ->
                if (cont.isActive) {
                    cont.resume(status) { _, _, _ -> }
                }
            }
            continuations.wifi = null

            updatePermissionStatus("Wifi", status)
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val status = if (isGranted) PermissionStatus.Granted else PermissionStatus.Denied

            continuations.camera?.let { cont ->
                if (cont.isActive) {
                    cont.resume(status) { _, _, _ -> }
                }
            }
            continuations.camera = null

            updatePermissionStatus("Camera", status)
        }

    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { _ ->
            // CheckAndRequestPermissions will inspect which permission we opened settings for
            // and resume the correct continuation.
            permissionHandler.onSettingsResult()
        }

    private val launchers = PermissionLaunchers(
        batteryOptimization = batteryOptimizationLauncher,
        fineAndCoarseLocation = requestFineAndCoarseLocationPermissionLauncher,
        postNotifications = requestPostNotificationsPermissionLauncher,
        phoneState = requestPhoneStatePermissionLauncher,
        backgroundLocation = requestBackgroundLocationPermissionLauncher,
        pauseAppWhenUnused = requestPauseAppWhenUnusedPermissionLauncher,
        camera = requestCameraPermissionLauncher,
        wifi = requestWifiPermissionLauncher,
        settings = settingsLauncher
    )

    private fun restorePermissionSteps() {
        val serializedSteps = SharedPrefsManager.getPermissionSteps(this)
        val restoredSteps = PermissionStepSerializer.deserialize(serializedSteps)
        permissionSteps.clear()
        permissionSteps.addAll(restoredSteps)
    }

    private fun checkAndRequestPermissions() {
        lifecycleScope.launch {

            val batteryIndex = permissionSteps.indexOfFirst { it.name == "Battery" }
            val locationIndex = permissionSteps.indexOfFirst { it.name == "Location" }
            val notificationsIndex = permissionSteps.indexOfFirst { it.name == "Notifications" }
            val phoneIndex = permissionSteps.indexOfFirst { it.name == "Phone" }
            val backgroundIndex = permissionSteps.indexOfFirst { it.name == "Background" }
            val pauseIndex = permissionSteps.indexOfFirst { it.name == "Pause" }
            val cameraIndex = permissionSteps.indexOfFirst { it.name == "Camera" }
            val wifiIndex = permissionSteps.indexOfFirst { it.name == "Wifi" }

            val batteryStatus = permissionSteps[batteryIndex].status
            val locationStatus = permissionSteps[locationIndex].status
            val notificationStatus = permissionSteps[notificationsIndex].status
            val phoneStatus = permissionSteps[phoneIndex].status
            val backgroundStatus = permissionSteps[backgroundIndex].status
            val pauseStatus = permissionSteps[pauseIndex].status
            val cameraStatus = permissionSteps[cameraIndex].status
            val wifiStatus = permissionSteps[wifiIndex].status

            //from last to first!
            if (wifiStatus == PermissionStatus.Granted || wifiStatus == PermissionStatus.Denied){
                return@launch
            }else{
                if (cameraStatus == PermissionStatus.Granted || cameraStatus == PermissionStatus.Denied){

                    if(!wifiChecked){
                        wifiChecked = true
                        if(enableDebugLogging) {
                            Log.d("PermissionsActivity", "running: checkAndRequestWifiPermission()")
                        }
                        permissionHandler.checkAndRequestWifiPermission()
                    }
                    return@launch
                }else if (pauseStatus == PermissionStatus.Granted || pauseStatus == PermissionStatus.Denied){
                    if(!cameraChecked){
                        cameraChecked = true
                        if(enableDebugLogging) {
                            Log.d(
                                "PermissionsActivity",
                                "running: checkAndRequestCameraPermission()"
                            )
                        }
                        permissionHandler.checkAndRequestCameraPermission()
                    }
                    return@launch
                }else if (backgroundStatus == PermissionStatus.Granted || backgroundStatus == PermissionStatus.Denied){
                    if(!pauseChecked){
                        pauseChecked = true
                        if(enableDebugLogging) {
                            Log.d(
                                "PermissionsActivity",
                                "running: checkAndRequestPauseAppWhenUnusedPermission()"
                            )
                        }
                        permissionHandler.checkAndRequestPauseAppWhenUnusedPermission()
                    }
                    return@launch
                }else if (phoneStatus == PermissionStatus.Granted || phoneStatus == PermissionStatus.Denied){
                    if(!backgroundChecked){
                        backgroundChecked = true
                        if(enableDebugLogging) {
                            Log.d(
                                "PermissionsActivity",
                                "running: checkAndRequestBackgroundPermission()"
                            )
                        }
                        permissionHandler.checkAndRequestBackgroundPermission()
                    }
                    return@launch
                }else if (notificationStatus == PermissionStatus.Granted || notificationStatus == PermissionStatus.Denied){
                    if(!phoneChecked){
                        phoneChecked = true
                        if(enableDebugLogging){
                            Log.d("PermissionsActivity","running: checkAndRequestPhonePermission()")
                        }
                        permissionHandler.checkAndRequestPhonePermission()
                    }
                    return@launch
                }else if ( locationStatus== PermissionStatus.Granted ||  locationStatus == PermissionStatus.Denied){
                    if(!notificationsChecked){
                        notificationsChecked = true
                        if(enableDebugLogging){
                            Log.d("PermissionsActivity","running: checkAndRequestNotificationPermission()")
                        }
                        permissionHandler.checkAndRequestNotificationPermission()
                    }
                    return@launch
                }else if ( batteryStatus== PermissionStatus.Granted ||  batteryStatus == PermissionStatus.Denied){
                    if(!locationChecked){
                        locationChecked = true
                        if(enableDebugLogging){
                            Log.d("PermissionsActivity","running: checkAndRequestLocationPermission()")
                        }
                        permissionHandler.checkAndRequestLocationPermission()
                    }
                    return@launch
                }else{
                    if(!batteryChecked){
                        batteryChecked = true
                        if(enableDebugLogging) {
                            Log.d(
                                "PermissionsActivity",
                                "running: checkAndRequestBatterOptimizationsPermission()"
                            )
                        }
                        permissionHandler.checkAndRequestBatteryOptimizationsPermission()
                    }
                    return@launch
                }
            }
        }
    }

    private fun savePermissionSteps() {
        val serializedSteps = PermissionStepSerializer.serialize(permissionSteps)
        SharedPrefsManager.setPermissionSteps(this, serializedSteps)
    }

    private fun updatePermissionStatus(permissionName: String, status: PermissionStatus) {
        if(enableDebugLogging) {
            Log.d("PermissionsActivity", "updatePermissionStatus: $permissionName: $status")
        }
        val index = permissionSteps.indexOfFirst { it.name == permissionName }
        if (permissionName == "Battery" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied)){
            if (!locationChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Battery" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied)
                    getString(R.string.toast_battery_permission_denied)
                else
                    getString(R.string.toast_battery_permission_granted)
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
            }
        }

        if (permissionName == "Location" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied)){
            if (!notificationsChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Location" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied)
                    getString(R.string.toast_location_permission_denied)
                else
                    getString(R.string.toast_location_permission_granted)
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
            }
        }

        if (permissionName == "Notifications" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied)){
            if (!phoneChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Notifications" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied)
                    getString(R.string.toast_notifications_permission_denied)
                else
                    getString(R.string.toast_notifications_permission_granted)
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
            }
        }


        if (permissionName == "Phone" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied)){
            if (!backgroundChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Phone" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied)
                    getString(R.string.toast_phone_permission_denied)
                else
                    getString(R.string.toast_phone_permission_granted)
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
            }
        }

        if (permissionName == "Background" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied) && savedInstanceStateOld == null){
            if (!pauseChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Background" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied) {
                    if (permissionHandler.checkAccessAndCoarseLocation(this@PermissionsActivity))
                        getString(R.string.toast_background_permission_denied_direct)
                    else
                        getString(R.string.toast_background_permission_denied_cascaded)
                } else {
                    getString(R.string.toast_background_permission_granted)
                }
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
                if (savedInstanceStateOld != null) {
                    if (!pauseChecked) {
                        permissionSteps[index] = permissionSteps[index].copy(status = status)
                        savePermissionSteps()
                        checkAndRequestPermissions()
                        return
                    }
                }
            }
        }

        if (permissionName == "Camera" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied)){
            if (!wifiChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Camera" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied)
                    getString(R.string.toast_camera_permission_denied)
                else
                    getString(R.string.toast_camera_permission_granted)
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
            }
        }


        if (permissionName == "Pause" && status == permissionSteps[index].status && (status == PermissionStatus.Granted || status == PermissionStatus.Denied)){
            if (!wifiChecked) {
                checkAndRequestPermissions()
            }
            return
        }else {
            if (permissionName == "Pause" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
                val message = if (status == PermissionStatus.Denied)
                    getString(R.string.toast_pause_permission_denied)
                else
                    getString(R.string.toast_pause_permission_granted)
                if(showPermissionStatusToast) {
                    lifecycleScope.launch {
                        snackbarHostState.showSnackbar(message)
                    }                }
                if(playSoundAfterPermissionGranted){
                    isPlaySoundEnabled = viewModel.getPlaySound()
                    if (isPlaySoundEnabled) {
                        if (status == PermissionStatus.Granted) {
                            soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                        } else {
                            soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                        }
                    }
                }
            }
        }

        if (permissionName == "Wifi" && (status == PermissionStatus.Denied || status == PermissionStatus.Granted)) {
            val message = if (status == PermissionStatus.Denied)
                getString(R.string.toast_wifi_permission_denied)
            else
                getString(R.string.toast_wifi_permission_granted)
            if(showPermissionStatusToast) {
                lifecycleScope.launch {
                    snackbarHostState.showSnackbar(message)
                }            }
            if(playSoundAfterPermissionGranted){
                isPlaySoundEnabled = viewModel.getPlaySound()
                if (isPlaySoundEnabled) {
                    if (status == PermissionStatus.Granted) {
                        soundPlayer.playSound(PermissionsStatusMp3.GRANT)
                    } else {
                        soundPlayer.playSound(PermissionsStatusMp3.DENIED)
                    }
                }
            }
            val allPermissionsGranted = permissionHandler.checkAllPermission(this@PermissionsActivity)

            if (allPermissionsGranted) {
                lifecycleScope.launch {
                    SharedPrefsManager.setCanServiceStart(this@PermissionsActivity, true)

                    permissionSteps[index] = permissionSteps[index].copy(status = status)
                    savePermissionSteps()
                    SharedPrefsManager.setPermissionsCheckedAfterInstallation(this@PermissionsActivity, false)
                    onPermissionWizardFinished()
                }
            } else {
                if(enableDebugLogging){
                    Log.e("MainActivity", "One or more permissions were not granted.")
                }
            }
        }

        permissionSteps[index] = permissionSteps[index].copy(status = status)
        savePermissionSteps()
    }

    private fun onReCheckAll(){
        // Reset all internal checked flags
        batteryChecked = false
        locationChecked = false
        notificationsChecked = false
        phoneChecked = false
        backgroundChecked = false
        pauseChecked = false
        cameraChecked = false
        wifiChecked = false

        // Also reset internal permissionStatuses
        permissionHandler.resetInternalState()

        // Reset permissionSteps in UI
        permissionSteps.clear()
        permissionSteps.addAll(
            listOf(
                PermissionStep("Battery", PermissionStatus.NotChecked),
                PermissionStep("Location", PermissionStatus.NotChecked),
                PermissionStep("Notifications", PermissionStatus.NotChecked),
                PermissionStep("Phone", PermissionStatus.NotChecked),
                PermissionStep("Background", PermissionStatus.NotChecked),
                PermissionStep("Pause", PermissionStatus.NotChecked),
                PermissionStep("Camera", PermissionStatus.NotChecked),
                PermissionStep("Wifi", PermissionStatus.NotChecked),

            )
        )

        // Persist reset state
        savePermissionSteps()

        // Start permission check process again
        checkAndRequestPermissions()
    }


    private fun afterResumeRun(){
        val batteryIndex = permissionSteps.indexOfFirst { it.name == "Battery" }
        val locationIndex = permissionSteps.indexOfFirst { it.name == "Location" }
        val notificationsIndex = permissionSteps.indexOfFirst { it.name == "Notifications" }
        val phoneIndex = permissionSteps.indexOfFirst { it.name == "Phone" }
        val backgroundIndex = permissionSteps.indexOfFirst { it.name == "Background" }
        val pauseIndex = permissionSteps.indexOfFirst { it.name == "Pause" }
        val cameraIndex = permissionSteps.indexOfFirst { it.name == "Camera" }
        val wifiIndex = permissionSteps.indexOfFirst { it.name == "Wifi" }

        val batteryStatus = permissionSteps[batteryIndex].status
        val locationStatus = permissionSteps[locationIndex].status
        val notificationStatus = permissionSteps[notificationsIndex].status
        val phoneStatus = permissionSteps[phoneIndex].status
        val backgroundStatus = permissionSteps[backgroundIndex].status
        val pauseStatus = permissionSteps[pauseIndex].status
        val cameraStatus = permissionSteps[cameraIndex].status
        val wifiStatus = permissionSteps[wifiIndex].status


        //from last to first!
        if (wifiStatus == PermissionStatus.Granted || wifiStatus == PermissionStatus.Denied) {
            if(enableDebugLogging) {
                Log.d(
                    "PermissionsActivity",
                    "afterResumeRun() call: Last permission Wifi Granted or Denied. Do nothing"
                )
            }
            if (permissionHandler.checkAllPermission(this@PermissionsActivity)) {
                SharedPrefsManager.setPermissionsCheckedAfterInstallation(this, false)
                onPermissionWizardFinished()
            }
            return
        } else {
            if (cameraStatus == PermissionStatus.Granted || cameraStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Camera Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            }else if (pauseStatus == PermissionStatus.Granted || pauseStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Pause activity Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            } else if (backgroundStatus == PermissionStatus.Granted || backgroundStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Background Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            } else if (phoneStatus == PermissionStatus.Granted || phoneStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Phone Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            } else if (notificationStatus == PermissionStatus.Granted || notificationStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Notifications Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            } else if (locationStatus == PermissionStatus.Granted || locationStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Location Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            } else if (batteryStatus == PermissionStatus.Granted || batteryStatus == PermissionStatus.Denied) {
                if (enableDebugLogging) {
                    Log.d(
                        "PermissionsActivity",
                        "afterResumeRun() call: Permission Battery Granted or Denied. CheckAndRequest for next permission."
                    )
                }
                checkAndRequestPermissions()
                return
            }
        }
    }

    @SuppressLint("UseKtx", "UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        enableDebugLogging = false
        showPermissionStatusToast = true
        playSoundAfterPermissionGranted = true

        savedInstanceStateOld = savedInstanceState
        super.onCreate(savedInstanceState)


        viewModel = ViewModelProvider(this)[PermissionViewModel::class.java]
        isPlaySoundEnabled = viewModel.getPlaySound()

        // Prevent the screen from turning off
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)


        playSoundAfterPermissionGrantedSetting = viewModel.getPlaySound()

        // If there is no saved instance state, clear the stored permission steps
        if (savedInstanceState == null) {
            SharedPrefsManager.clearPermissionSteps(this)
        }

        enableEdgeToEdge()

        val shouldReset = intent?.getBooleanExtra("force_permission_reset", false) == true


        permissionHandler.initializePermissions(
            context = this,
            coroutineScope = lifecycleScope,
            continuations = continuations,
            launchers = launchers,
            updatePermissionStatus = { permissionName, status ->
                updatePermissionStatus(permissionName, status)
            },
            shouldReset = shouldReset
        )

        // ✅ Only restore steps if no intent extra is present
        if (shouldReset && savedInstanceStateOld == null) {
            permissionSteps.clear()
            permissionSteps.addAll(
                listOf(
                    PermissionStep("Battery", PermissionStatus.NotChecked),
                    PermissionStep("Location", PermissionStatus.NotChecked),
                    PermissionStep("Notifications", PermissionStatus.NotChecked),
                    PermissionStep("Phone", PermissionStatus.NotChecked),
                    PermissionStep("Background", PermissionStatus.NotChecked),
                    PermissionStep("Pause", PermissionStatus.NotChecked),
                    PermissionStep("Camera", PermissionStatus.NotChecked),
                    PermissionStep("Wifi", PermissionStatus.NotChecked),

                )
            )
            SharedPrefsManager.clearPermissionSteps(this) // 🧹 clear stored progress
        } else {
            restorePermissionSteps()

        }


        // ✅ Remove the "force_permission_reset" extra so it doesn't persist
        intent?.removeExtra("force_permission_reset")

        if (permissionSteps.isEmpty()) {
            permissionSteps.addAll(
                listOf(
                    PermissionStep("Battery", PermissionStatus.NotChecked),
                    PermissionStep("Location", PermissionStatus.NotChecked),
                    PermissionStep("Notifications", PermissionStatus.NotChecked),
                    PermissionStep("Phone", PermissionStatus.NotChecked),
                    PermissionStep("Background", PermissionStatus.NotChecked),
                    PermissionStep("Pause", PermissionStatus.NotChecked),
                    PermissionStep("Camera", PermissionStatus.NotChecked),
                    PermissionStep("Wifi", PermissionStatus.NotChecked),


                )
            )
        }

        setContent {
            MobeetestTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState) { data ->
                            CustomSnackbar(
                                message = data.visuals.message,
                                onDismiss = { data.dismiss() }
                            )
                        }
                    }
                ) {

                    // 🔥 ΠΡΟΣΘΕΣΕ ΤΟ ΕΔΩ — ΔΙΑΒΑΖΕΙ ΤΟ DIALOG STATE
                    val dialogState = permissionHandler.dialogState.collectAsState().value

                    // 🔥 ΖΩΓΡΑΦΙΖΕΙ ΤΟ CUSTOM ALERT DIALOG
                    if (dialogState != null) {
                        BorderedAlertDialog(
                            title = dialogState.title,
                            message = dialogState.message,
                            positiveButtonText = dialogState.positiveButtonText,
                            cancelable = dialogState.cancelable,
                            onPositiveClick = dialogState.onPositiveClick,
                            onDismiss = {
                                permissionHandler.clearDialog()
                            }
                        )
                    }

                    PermissionsScreen(
                        permissionSteps = permissionSteps,
                        onReCheckAll = { onReCheckAll() },
                        onSkipPermissionCheck = {
                            intent.removeExtra("force_permission_reset")
                            SharedPrefsManager.setPermissionsCheckedAfterInstallation(this, false)
                            onPermissionWizardFinished()
                        },
                        viewModel = viewModel
                    )
                }
            }
        }

        // Initialize permissions in CheckAndRequestPermissions
        if (savedInstanceStateOld == null) {
            if(shouldReset){
                permissionSteps.clear()
                permissionSteps.addAll(
                    listOf(
                        PermissionStep("Battery", PermissionStatus.NotChecked),
                        PermissionStep("Location", PermissionStatus.NotChecked),
                        PermissionStep("Notifications", PermissionStatus.NotChecked),
                        PermissionStep("Phone", PermissionStatus.NotChecked),
                        PermissionStep("Background", PermissionStatus.NotChecked),
                        PermissionStep("Pause", PermissionStatus.NotChecked),
                        PermissionStep("Camera", PermissionStatus.NotChecked),
                        PermissionStep("Wifi", PermissionStatus.NotChecked),

                    )
                )
            }
            if(enableDebugLogging){
                Log.d("PermissionsActivity", "onCreate() call: $permissionSteps")
            }
            checkAndRequestPermissions()
        }
        // 🟩 Add this block after setContent
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = true

        @Suppress("DEPRECATION")
        window.statusBarColor = ContextCompat.getColor(this, R.color.transparent)
    }

    public override fun onResume() {
        super.onResume()
        afterResumeRun()
    }

    public override fun onDestroy() {
        SharedPrefsManager.setPermissionsCheckedAfterInstallation(this, false)
        soundPlayer.release()
        super.onDestroy()
    }

}

object PermissionStepSerializer {
    private const val SEPARATOR = ","
    private const val INNER_SEPARATOR = ":"

    fun serialize(permissionSteps: List<PermissionStep>): String {
        return permissionSteps.joinToString(separator = SEPARATOR) { "${it.name}$INNER_SEPARATOR${it.status.name}" }
    }

    fun deserialize(serializedSteps: String): List<PermissionStep> {
        if (serializedSteps.isEmpty()) {
            return emptyList()
        }
        return serializedSteps.split(SEPARATOR).map { serializedStep ->
            val parts = serializedStep.split(INNER_SEPARATOR)
            val name = parts[0]
            val status = PermissionStatus.valueOf(parts[1])
            PermissionStep(name, status)
        }
    }

}