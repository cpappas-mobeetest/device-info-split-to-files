package com.mobeetest.worker

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.lifecycle.AndroidViewModel
import com.mobeetest.worker.services.location.LocationService
import com.mobeetest.worker.services.network.NetworkService
import com.mobeetest.worker.services.notification.NotificationService
import com.mobeetest.worker.services.sensors.SensorsService
import com.mobeetest.worker.ui.activities.main.scaffold.CachedNavigationApp
import com.mobeetest.worker.ui.theme.MobeetestTheme
import com.mobeetest.worker.utils.permissions.CheckAndRequestPermissions
import com.mobeetest.worker.viewModels.DeviceInfoViewModel
import com.mobeetest.worker.sharedPreferences.permissions.SharedPrefsManager

@ExperimentalFoundationApi
class MainActivity : ComponentActivity() {

    private var servicesStarted = false

    companion object {
        const val EXTRA_PERMISSIONS_CHECK_COMPLETED = "extra_permissions_check_completed"
    }

    private var permissionHandler: CheckAndRequestPermissions = CheckAndRequestPermissions()

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            // Αυτό εκτελείται όταν γίνει finish το PermissionsActivity
            if (result.resultCode == RESULT_OK) {
                val completed = result.data
                    ?.getBooleanExtra(EXTRA_PERMISSIONS_CHECK_COMPLETED, false)
                    ?: false

                if (completed && hasAllCorePermissionsGranted() && !servicesStarted) {
                    startAllServicesAndReceivers()
                    servicesStarted = true
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MobeetestTheme {
                val deviceInfoViewModel: DeviceInfoViewModel by viewModels()
                val viewModels: List<AndroidViewModel> = listOf(deviceInfoViewModel)
                CachedNavigationApp(viewModels,
                    onRecheckPermissions = { handleRecheckPermissions() }
                )
            }
        }

        // Αν ΔΕΝ έχουν δοθεί όλα τα core permissions → άνοιξε PermissionsActivity
        if (!hasAllCorePermissionsGranted()) {
            openPermissionsActivity()
        } else {
            // Αν είναι ήδη granted (π.χ. 2η/3η φορά που ανοίγει η εφαρμογή)
            startAllServicesIfNeeded()
        }
    }

    private fun handleRecheckPermissions() {
        // 1. Καθαρίζουμε όλα τα permission-related prefs
        SharedPrefsManager.clearAllPermissionData(this)

        // 2. Μηδενίζουμε in-memory state
        servicesStarted = false

        // 3. Restart της MainActivity σε νέο task
        val restartIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        startActivity(restartIntent)
        // 4. Κλείνουμε την τρέχουσα (θα κλείσει όλο το παλιό task)
        finish()
    }



    private fun openPermissionsActivity() {
        val intent = Intent(this, PermissionsActivity::class.java).apply {
            putExtra("force_permission_reset", true)
        }
        permissionsLauncher.launch(intent)
    }

    private fun startAllServicesIfNeeded() {
        if (!servicesStarted && hasAllCorePermissionsGranted()) {
            startAllServicesAndReceivers()
            servicesStarted = true
        }
    }

    //ΕΔΩ ΤΑ ΘΕΩΡΩ ΟΛΑ CORE ΓΙΑ ΑΥΤΟ ΓΚΡΙΝΙΑΖΕΙ ΑΝ ΔΕΝ ΤΟΥ ΔΩΣΕΙΣ ΚΑΤΙ ΣΤΗΝ ΑΡΧΗ
    private fun hasAllCorePermissionsGranted(): Boolean {
        return permissionHandler.checkAllPermission(this@MainActivity)
    }

    private fun startAllServicesAndReceivers() {
        val runIntent = Intent(this, LocationService::class.java).apply { action = "RUN" }
        val runSensorsIntent = Intent(this, SensorsService::class.java).apply { action = "RUN" }
        val runNetworkIntent = Intent(this, NetworkService::class.java).apply { action = "RUN" }
        val notificationServiceIntent = Intent(this, NotificationService::class.java)

        startForegroundService(notificationServiceIntent)
        startService(runIntent)
        startService(runSensorsIntent)
        startService(runNetworkIntent)
    }
}
